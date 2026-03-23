package com.transaction.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transaction.domain.entity.SettlementItem;
import com.transaction.domain.enums.SettlementStatus;
import com.transaction.domain.repository.SettlementItemRepository;
import com.transaction.dto.event.SettlementEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

import java.util.Random;

//只要 SettlementConsumer 有掛 @Component 且內部有 @KafkaListener，Spring 一啟動就會自動連上 Kafka 開始待命。

@Slf4j
@Component
public class SettlementConsumer {
    private static final Logger log = LoggerFactory.getLogger(SettlementConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    SettlementItemRepository itemRepo;

    private final Random random = new Random();

    // 這裡的 topics 必須跟 Publisher 發送的名稱一致
    // groupId 決定了哪些 Consumer 屬於同一個團隊 (Team)
    @KafkaListener(topics = "settlement-events", groupId = "settlement-group")
    public void consume(String message) {
        try {
            log.info("📥 Received Kafka message: {}", message);

            // 1. 解析 JSON
            SettlementEvent event = objectMapper.readValue(message, SettlementEvent.class);

            // 2. 執行真正的業務邏輯 (例如呼叫外部銀行 API 或更新帳務)
            SettlementItem item = itemRepo.findById(event.getId()).orElseThrow();


            boolean isSuccess = simulateExternalCall();

            if (isSuccess) {
                item.setStatus(SettlementStatus.SUCCESS.getCode());
            } else {
                item.setStatus(SettlementStatus.FAILED.getCode());
            }
            itemRepo.save(item);

            log.info("✅ Successfully processed event for item: {}", event.getId());
        } catch (Exception e) {
            // 如果失敗，Kafka 預設會根據你的配置進行重試 (Retry)
            log.error(" Failed to process Kafka message", e);
        }
    }

    public boolean simulateExternalCall() {
        // 80% 成功，20% 失敗
        return random.nextInt(10) < 8;
    }


}
