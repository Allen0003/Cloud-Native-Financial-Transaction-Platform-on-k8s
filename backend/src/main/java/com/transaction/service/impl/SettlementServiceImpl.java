package com.transaction.service.impl;

import com.transaction.domain.entity.SettlementBatch;
import com.transaction.domain.repository.SettlementBatchRepository;
import com.transaction.domain.repository.SettlementItemRepository;
import com.transaction.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementBatchRepository batchRepo;
    private final SettlementItemRepository itemRepo;

    public SettlementServiceImpl(SettlementBatchRepository batchRepo, SettlementItemRepository itemRepo) {
        this.batchRepo = batchRepo;
        this.itemRepo = itemRepo;
    }


    @Transactional
    public void runBatch(LocalDate date) {

        // 1️⃣ 建立或取得 batch（idempotent）
        SettlementBatch batch = batchRepo.findByBatchDate(date)
                .orElseGet(() -> {
                    SettlementBatch b = new SettlementBatch();
                    b.setBatchDate(date);
                    b.setStatus("PENDING");
                    return batchRepo.save(b);
                });

        // 2️⃣ 如果已完成 → 直接跳過
        if ("COMPLETED".equals(batch.getStatus())) {
            return;
        }

        // 3️⃣ 更新為 PROCESSING
        batch.setStatus("PROCESSING");
        batch.setStartedAt(LocalDateTime.now());
        batchRepo.save(batch);

        // 4️⃣ 寫入 settlement_item（idempotent）
        itemRepo.insertSettlementItems(batch.getId());

        // 5️⃣ TODO: 處理 item（下一步會做）

        // 6️⃣ 更新 batch 完成
        batch.setStatus("COMPLETED");
        batch.setCompletedAt(LocalDateTime.now());
        batchRepo.save(batch);
    }

}
