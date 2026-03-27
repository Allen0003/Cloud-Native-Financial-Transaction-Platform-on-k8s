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

    public void batchProcessLevelSettlement() {
        log.info(">>> Starting Batch Settlement Process...");

        // 1. 定義查詢條件：撈出所有需要計算的 User
        Query query = new Query();
        // 設定 batchSize，這就是告訴 MongoDB 驅動：每次從網路抓 10 筆回來就好，不要一次抓 100 筆
        query.cursorBatchSize(batchSize);

        int totalProcessed = 0;
        List<User> chunkBuffer = new ArrayList<>();

        // 2. 使用 stream (流式讀取)：這不會一次把 100 筆塞進 JVM 記憶體
        try (CloseableIterator<User> iterator = mongoTemplate.stream(query, User.class)) {
            while (iterator.hasNext()) {
                User user = iterator.next();

                // --- 計算邏輯：money % 3 分成 level 1, 2, 3 ---
                int calculatedLevel = (int) (user.getMoney() % 3) + 1;
                user.setLevel(calculatedLevel);

                chunkBuffer.add(user);
                totalProcessed++;

                // 3. 每滿 10 筆，發動一次 Bulk Update
                if (chunkBuffer.size() >= 10) {
                    executeBulkUpdate(chunkBuffer);
                    chunkBuffer.clear(); // 清空緩衝區，準備下一批

                    // --- 睡眠 5 秒測試邏輯 ---
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Batch sleep interrupted", e);
                    }



                    log.info("Processed {} records so far...", totalProcessed);
                }
            }

            // 4. 處理剩下的碎末 (例如最後剩下 3 筆不滿 10 筆的情況)
            if (!chunkBuffer.isEmpty()) {
                executeBulkUpdate(chunkBuffer);
            }
        }

        log.info(">>> Finished! Total records processed: {}", totalProcessed);




    }

    private void executeBulkUpdate(List<User> users) {
        // 使用 UNORDERED 模式：讓 Mongos 同時把更新丟給 Shard 1 和 Shard 2，效率最高
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);

        for (User user : users) {
            // 注意：更新時務必帶著 Shard Key (userid)，否則 Mongos 會廣播到所有 Shard，效能變差
            Query updateQuery = new Query(Criteria.where("userid").is(user.getUserId()));
            Update update = new Update().set("level", user.getLevel());

            bulkOps.updateOne(updateQuery, update);
        }

        // 一口氣把 10 筆更新送出去
        bulkOps.execute();
    }

}
