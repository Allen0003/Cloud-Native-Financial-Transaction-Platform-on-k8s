package com.transaction.service.impl;


import com.transaction.domain.entity.User;
import com.transaction.service.SettlementMongoService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator; // 注意是在 .util 下
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;

import javax.annotation.PostConstruct;


@Slf4j
@Service
public class SettlementMongoServiceImpl implements SettlementMongoService {
    private static final Logger log = LoggerFactory.getLogger(SettlementMongoServiceImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.batch.size:10}")
    private int batchSize;

    @Value("${app.pod.name:local-dev-0}") // 預設給個帶數字的名字方便本地測試
    private String podName;

    private int podIndex;

    // K8s 注入：總共有幾個 Pod 正在跑
    @Value("${app.pod.replicas:1}")
    private int totalPods;

    @Value("${app.kafka.dlq-topic:settlement-dlq}")
    private String dlqTopic;

    // 定義要開幾個執行緒（例如 3 個）
    private final int threadCount = 3;

    // 建立執行緒池
    private final ExecutorService executor = Executors.newFixedThreadPool(threadCount);


    @PostConstruct
    public void init() {
        try {
            // 從名字最後一個字抓數字 (例如 financial-tx-1 -> 1)
            String indexStr = podName.substring(podName.lastIndexOf("-") + 1);
            this.podIndex = Integer.parseInt(indexStr);
            log.info("[System Init] Detected Pod Index: {} from Pod Name: {}", this.podIndex, podName);
        } catch (Exception e) {
            log.warn("[System Init] Could not parse Index from Pod Name {}, defaulting to 0", podName);
            this.podIndex = 0;
        }
    }

    @Override
    public void batchProcessLevelSettlement() {
        // 計算總分片數 (例如 2 Pods * 3 Threads = 6)
        int globalTotalPartitions = totalPods * threadCount;
        log.info("Starting Batch: PodIndex={}, Replicas={}, GlobalPartitions={}", podIndex, totalPods, globalTotalPartitions);

        for (int i = 0; i < threadCount; i++) {
            int globalRemainder = (podIndex * threadCount) + i;
            executor.submit(() -> {
                try {
                    processPartition(globalRemainder, globalTotalPartitions);
                } catch (Exception e) {
                    log.error("Thread for remainder {} failed", globalRemainder, e);
                }
            });
        }
    }

    private void processPartition(int globalRemainder, int globalTotalPartitions) {
        String threadId = "Global-Part-" + globalRemainder;
        log.info("[{}] Started. Query: money % {} == {}", threadId, globalTotalPartitions, globalRemainder);

        // 3. 關鍵查詢：使用全局分片邏輯，確保 Pod 之間不重疊
        Query query = new Query(Criteria.where("money").mod(globalTotalPartitions, globalRemainder));
        query.cursorBatchSize(batchSize);

        int processedInThread = 0;
        List<User> buffer = new ArrayList<>();

        try (CloseableIterator<User> iterator = mongoTemplate.stream(query, User.class)) {
            while (iterator.hasNext()) {
                User user = iterator.next();


                // --- 單筆資料處理保護區 ---
                try {
                    // 模擬處理邏輯 (假設 money 為空或格式異常會噴 Exception)
                    if (user.getMoney() == 7777) {
                        throw new RuntimeException("Money field is null");
                    }

                    user.setLevel((int) (user.getMoney() % 3) + 1);
                    buffer.add(user);
                    processedInThread++;

                } catch (Exception e) {
                    log.info("[{}] GG  {}", user, e.getMessage());
                    // 發生錯誤，直接丟往 Kafka DLQ，不中斷迴圈
                    sendToDlq(user, e.getMessage(), threadId);
                }

//                user.setLevel((int) (user.getMoney() % 3) + 1);
//                buffer.add(user);
//                processedInThread++;

                if (buffer.size() >= batchSize) {
                    executeBulkUpdate(buffer, threadId);
                    log.info("[{}] Processed total: {}", threadId, processedInThread);
                    buffer.clear();
                    Thread.sleep(5000);
                }
            }
            if (!buffer.isEmpty()) executeBulkUpdate(buffer, threadId);
        } catch (Exception e) {
            log.error("[{}] Error", threadId, e);
        }
        log.info("[{}] Finished. Total: {}", threadId, processedInThread);
    }


    /**
     * 將處理失敗的資料丟入 Kafka DLQ
     */
    private void sendToDlq(User user, String errorMessage, String threadId) {
        Map<String, Object> errorPayload = new HashMap<>();
        errorPayload.put("userData", user.getUserId()); // 包含原始資料
        errorPayload.put("error", errorMessage);
        errorPayload.put("pod", podName);
        errorPayload.put("thread", threadId);
        errorPayload.put("timestamp", new Date());

        // 使用 Kafka 發送，key 使用 userId 方便追蹤
        kafkaTemplate.send(dlqTopic, String.valueOf(user.getId()), errorPayload.get("userData").toString())
                .addCallback(
                        result -> log.debug("Sent error data to DLQ for user: {}", user.getId()),
                        ex -> log.error("Failed to send data to Kafka DLQ! User: {}", user.getId(), ex)
                );
    }


    private void executeBulkUpdate(List<User> users, String threadId) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);
        for (User user : users) {
            // 務必帶上 Shard Key: userid
            Query updateQuery = new Query(Criteria.where("Id").is(user.getId()));
            Update update = new Update()
                    .set("level", user.getLevel())
                    .set("processed_pod", podName)
                    .set("processed_thread", threadId)
                    .set("processed_at", new Date()); // 加個時間戳更好查
            bulkOps.updateOne(updateQuery, update);
        }
        bulkOps.execute();
    }
}
