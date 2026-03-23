package com.transaction.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transaction.dto.event.SettlementEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;


@Slf4j
@Component
public class SettlementConsumer {
    private static final Logger log = LoggerFactory.getLogger(SettlementConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    // 這裡的 topics 必須跟 Publisher 發送的名稱一致
    // groupId 決定了哪些 Consumer 屬於同一個團隊 (Team)
    @KafkaListener(topics = "settlement-events", groupId = "settlement-group")
    public void consume(String message) {
        try {
            log.info("📥 Received Kafka message: {}", message);

            // 1. 解析 JSON
            SettlementEvent event = objectMapper.readValue(message, SettlementEvent.class);

            // 2. 執行真正的業務邏輯 (例如呼叫外部銀行 API 或更新帳務)
            processSettlement(event);

            log.info("✅ Successfully processed event for item: {}", event.getId());
        } catch (Exception e) {
            // 如果失敗，Kafka 預設會根據你的配置進行重試 (Retry)
            log.error(" Failed to process Kafka message", e);
        }
    }

    private void processSettlement(SettlementEvent event) {
        // 這裡寫入你最終的結算邏輯
        // 例如：呼叫財金公司、發送簡訊通知、或是更新最後的會計分錄
    }
}
