package com.transaction.service.impl;


import com.transaction.domain.entity.User;
import com.transaction.service.SettlementMongoService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator; // 注意是在 .util 下
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;


@Slf4j
@Service
public class SettlementMongoServiceImpl implements SettlementMongoService {
    private static final Logger log = LoggerFactory.getLogger(SettlementMongoServiceImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${app.batch.size:10}")
    private int batchSize;

    @Value("${app.pod.name:local-dev}")
    private String podName;


    // 定義要開幾個執行緒（例如 3 個）
    private final int threadCount = 3;

    // 建立執行緒池
    private final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    @Override
    public void batchProcessLevelSettlement() {
        log.info("~~~~~~~~~~ Starting Multi-threaded Batch Process (Threads: {})", threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int remainder = i;
            executor.submit(() -> {
                try {
                    processPartition(remainder);
                } catch (Exception e) {
                    log.error("Thread for remainder {} failed", remainder, e);
                }
            });
        }
    }

    private void processPartition(int remainder) {
        log.info("[Thread-{}] Started. Handling money % {} == {}", remainder, threadCount, remainder);

        String threadId = "Thread-" + remainder;


        // 使用 MongoDB 的 $mod 運算子進行分區查詢
        // 語法：{ field: { $mod: [ divisor, remainder ] } }
        Query query = new Query(Criteria.where("money").mod(threadCount, remainder));
        query.cursorBatchSize(batchSize);

        int processedInThread = 0;
        List<User> buffer = new ArrayList<>();

        try (CloseableIterator<User> iterator = mongoTemplate.stream(query, User.class)) {
            while (iterator.hasNext()) {
                User user = iterator.next();

                // 計算邏輯
                user.setLevel((int) (user.getMoney() % 3) + 1);
                buffer.add(user);
                processedInThread++;

                if (buffer.size() >= batchSize) {
                    executeBulkUpdate(buffer, threadId);
                    log.info("[Thread-{}] Bulk updated {} records. (Total: {})",
                            remainder, buffer.size(), processedInThread);
                    buffer.clear();


                    Thread.sleep(5000);
                }
            }
            // 最後一聲哨音
            if (!buffer.isEmpty()) {
                executeBulkUpdate(buffer, threadId);
            }
        } catch (Exception e) {
            log.error("[Thread-{}] Error during streaming", remainder, e);
        }

        log.info("[Thread-{}] Finished. Total processed: {}", remainder, processedInThread);
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
