package com.transaction.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transaction.domain.entity.OutboxEvent;
import com.transaction.domain.entity.SettlementBatch;
import com.transaction.domain.entity.SettlementItem;
import com.transaction.domain.enums.SettlementStatus;

import com.transaction.domain.repository.*;
import com.transaction.service.SettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SettlementKafkaServiceImpl implements SettlementService {
    private static final Logger log = LoggerFactory.getLogger(SettlementKafkaServiceImpl.class);

    @Autowired
    SettlementBatchRepository batchRepo;

    @Autowired
    SettlementItemRepository itemRepo;

    @Autowired
    OutboxRepository outboxRepo; // 新增：用來存發送給 Kafka 的事件

    @Autowired
    ObjectMapper objectMapper; // 用來將物件轉成 JSON 字串

    public void runBatch(LocalDate date) {
        ensureBatchExists(date);

        // 嘗試搶鎖
        int locked = batchRepo.tryLockBatchWithTimeout(date, LocalDateTime.now().minusMinutes(1));
        if (locked == 0) return;

        log.info("I got lock, starting batch processing...");

        try {
            SettlementBatch currentBatch = batchRepo.findByBatchDate(date).orElseThrow();

            // 執行冪等性插入
            itemRepo.insertSettlementItems(currentBatch.getId());

            // 處理每一筆結算項目
            processItems(currentBatch);

            // 更新為完成
            currentBatch.setStatus(SettlementStatus.COMPLETED.getCode());
            currentBatch.setCompletedAt(LocalDateTime.now());
            batchRepo.save(currentBatch);

        } catch (Exception e) {
            log.error("Batch processing failed", e);
            throw e;
        }
    }

    private void ensureBatchExists(LocalDate date) {
        try {
            batchRepo.findByBatchDate(date).orElseGet(() -> {
                SettlementBatch b = new SettlementBatch();
                b.setBatchDate(date);
                b.setStatus(SettlementStatus.PENDING.getCode());
                return batchRepo.saveAndFlush(b);
            });
        } catch (DataIntegrityViolationException e) {
            // 已被其他節點建立
        }
    }

    public void processItems(SettlementBatch batch) {
        List<SettlementItem> items = itemRepo.findPendingItems(batch.getId());
        for (SettlementItem item : items) {
            try {
                // 這裡會呼叫有 @Transactional 的方法
                processSingleItem(item);
            } catch (Exception e) {
                log.error("Failed to process item: {}", item.getId(), e);
            }
        }
    }

    /**
     * Outbox Pattern 核心：
     * 將資料更新與訊息記錄綁在同一個 Transaction 內
     */
    @Transactional
    public void processSingleItem(SettlementItem item) {
        // 1. 更新項目狀態為已處理（或處理中）
        item.setStatus(SettlementStatus.SUCCESS.getCode());
        itemRepo.save(item);

        // 2. 建立 Outbox 事件 (不直接發 Kafka)
        try {
            String payload = objectMapper.writeValueAsString(item);

            OutboxEvent event = new OutboxEvent();
            event.setAggregateId(item.getId().toString());
            event.setEventType("SETTLEMENT_COMPLETED");
            event.setPayload(payload);
            event.setStatus("NEW"); // 初始狀態為待傳送
            event.setCreatedAt(LocalDateTime.now());

            outboxRepo.save(event); // 存入 DB，若上面的 save(item) 失敗，這筆也會回滾

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize item to JSON", e);
            throw new RuntimeException("Serialization failed");
        }
    }

    public boolean simulateExternalCall() {
        return false;
    }
}