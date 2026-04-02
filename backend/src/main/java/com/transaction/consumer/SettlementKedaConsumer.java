package com.transaction.consumer;

import com.transaction.domain.entity.User;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;

@Service
public class SettlementKedaConsumer {

    private static final Logger log = LoggerFactory.getLogger(SettlementKedaConsumer.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.pod.name:local-dev-0}") // 預設給個帶數字的名字方便本地測試
    private String podName;


    @PostConstruct
    public void init() {
        log.info(">>>>>> [DEBUG] Kafka Consumer 正在啟動，準備監聽 Topic: settlement-topic3, Group: financial-group1");
    }

    // KEDA 模式下，建議直接由 Kafka Listener 觸發，而非由外部排程觸發 batch
    @KafkaListener(topics = "settlement-topic3", groupId = "financial-group1", concurrency = "3")
    public void onMessage(ConsumerRecord<String, String> record) {
        String userId = record.value();
        log.info("[KEDA-Process] Received User: {}", userId);

        try {
            updateUserLevel(userId);
        } catch (Exception e) {
            log.error("[KEDA-Error] Failed to process UserID: {}", userId, e);
        }
    }


    private void updateUserLevel(String userId) {
        // 1. 定義查詢條件 (務必匹配你的 Shard Key)
        Query query = new Query(Criteria.where("Id").is(userId));

        // 2. 模擬結算邏輯 (這裡先抓出 User 資料來計算 Level)
        User user = mongoTemplate.findOne(query, User.class);
        if (user == null) {
            log.warn("User {} not found in MongoDB", userId);
            return;
        }

        // 3. 計算新的 Level (範例邏輯)
        int newLevel = (int) (user.getMoney() % 3) + 1;

        // 4. 執行精準更新 (與你之前的 BulkUpdate 邏輯一致)
        Update update = new Update()
                .set("level", newLevel)
                .set("processed_pod", podName)
                .set("processed_method", "KEDA-EVENT-DRIVEN")
                .set("processed_at", new Date());

        // 執行更新
        mongoTemplate.updateFirst(query, update, User.class);
        log.info("[KEDA-Success] User {} updated to Level {}", userId, newLevel);
    }
}
