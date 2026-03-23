
//唯一任務就是：把要做的任務（Task）安全地存進資料庫，並產生一張「待辦清單」（Outbox），交給後續的 Kafka 流程去跑。
//這支程式現在只做三件事，這三件事必須在同一個資料庫交易（Transaction）裡：
//
//1. 確保 Batch 存在：今天有沒有這筆批次？沒有就建一個。
//2. 建立 Items：把今天要算的帳（例如 100 筆交易）全部塞進 settlement_item 表，狀態設為 PENDING。
//3. 發送任務（核心）：把這 100 筆 ID 轉成 OutboxEvent 存起來。


package com.transaction.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transaction.domain.entity.OutboxEvent;
import com.transaction.domain.entity.SettlementBatch;
import com.transaction.domain.entity.SettlementItem;
import com.transaction.domain.enums.OutboxStatus;
import com.transaction.domain.enums.SettlementStatus;
import com.transaction.domain.repository.OutboxRepository;
import com.transaction.domain.repository.SettlementBatchRepository;
import com.transaction.domain.repository.SettlementItemRepository;
import com.transaction.service.SettlementKafkaService;
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
public class SettlementKafkaServiceImpl implements SettlementKafkaService {
    private static final Logger log = LoggerFactory.getLogger(SettlementKafkaServiceImpl.class);

    @Autowired
    SettlementBatchRepository batchRepo;

    @Autowired
    SettlementItemRepository itemRepo;

    @Autowired
    OutboxRepository outboxRepo; // 新增：用來存發送給 Kafka 的事件

    @Autowired
    ObjectMapper objectMapper; // 用來將物件轉成 JSON 字串

    @Transactional
    public void runBatch(LocalDate date) {

        log.info("run batch in SettlementKafkaServiceImpl ~~~~ ");

        // 1. 取得或建立今天的批次
        SettlementBatch batch = ensureBatchExists(date);

        // 2. 寫入待處理項目 (冪等性：如果已經有資料就不會重覆插入)
        itemRepo.insertSettlementItems(batch.getId());

        // 3. 找出所有還沒處理的項目，發送到 Outbox
        dispatchItemsToOutbox(batch.getId());

    }

    private void dispatchItemsToOutbox(Long batchId) {
        // 撈出該批次所有 PENDING 的項目
        List<SettlementItem> items = itemRepo.findPendingItems(batchId);

        for (SettlementItem item : items) {
            try {
                // 將物件轉為 JSON
                String payload = objectMapper.writeValueAsString(item);

                // 建立 Outbox 紀錄
                OutboxEvent event = new OutboxEvent();
                event.setAggregateId(item.getId().toString());
                event.setEventType("SETTLEMENT_TASK_CREATED");
                event.setPayload(payload);
                event.setStatus(OutboxStatus.NEW.getCode()); // 使用你剛剛建的 Enum
                event.setCreatedAt(LocalDateTime.now());

                outboxRepo.save(event);

                // 同步標記 Item 狀態為 PROCESSING，避免被重複掃描
                item.setStatus("PROCESSING");
                itemRepo.save(item);

            } catch (JsonProcessingException e) {
                log.error("Failed to serialize item {}", item.getId(), e);
                // 這裡可以選擇丟出 RuntimeException 讓整個 Transaction 回滾
            }
        }
    }


    private SettlementBatch ensureBatchExists(LocalDate date) {
        return batchRepo.findByBatchDate(date).orElseGet(() -> {
            try {
                SettlementBatch b = new SettlementBatch();
                b.setBatchDate(date);
                b.setStatus(SettlementStatus.PENDING.getCode());
                return batchRepo.saveAndFlush(b);
            } catch (DataIntegrityViolationException e) {
                // 併發處理：如果剛好別的節點建好了，就再抓一次
                return batchRepo.findByBatchDate(date).orElseThrow();
            }
        });
    }

}