package com.transaction.publisher;

import com.transaction.domain.entity.OutboxEvent;
import com.transaction.domain.repository.OutboxRepository;
import com.transaction.service.impl.SettlementServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component // 讓 Spring 啟動這個零件
@EnableScheduling // 開啟定時任務功能
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(SettlementServiceImpl.class);

    @Autowired
    private OutboxRepository repo;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // 每 5 秒執行一次
    @Scheduled(fixedDelay = 5000)
    public void publishOutbox() {
        // 1. 每次只抓 10 筆，避免一次塞爆記憶體
        List<OutboxEvent> events = repo.findTop10ByStatus("NEW");

        for (OutboxEvent e : events) {
            try {
                // 2. 真正發送到 Kafka (Topic 叫 settlement-events)
                kafkaTemplate.send("settlement-events", e.getAggregateId(), e.getPayload());

                // 3. 成功後才改狀態
                e.setStatus("SENT");
                repo.save(e);

                log.info("Successfully sent event: {}", e.getAggregateId());
            } catch (Exception ex) {
                // 4. 如果 Kafka 壞了或網路斷了，這邊會噴錯，status 停在 NEW
                // 下一個 5 秒鐘巡邏時，會再次嘗試發送 (保證不漏傳)
                log.error("Kafka send failed, will retry later", ex);
            }
        }
    }
}
