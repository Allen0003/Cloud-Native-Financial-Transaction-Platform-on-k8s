package com.transaction.service.impl;

import com.transaction.domain.entity.SettlementBatch;
import com.transaction.domain.entity.SettlementItem;
import com.transaction.domain.repository.SettlementBatchRepository;
import com.transaction.domain.repository.SettlementItemRepository;
import com.transaction.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;


@Service

public class SettlementServiceImpl implements SettlementService {

    private final SettlementBatchRepository batchRepo;
    private final SettlementItemRepository itemRepo;

    private final Random random = new Random();


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


    public void processItems(SettlementBatch batch) {

        List<SettlementItem> items =
                itemRepo.findPendingItems(batch.getId());

        for (SettlementItem item : items) {
            try {
                // 模擬外部銀行 API
                boolean success = simulateExternalCall();

                if (success) {
                    item.setStatus("SUCCESS");
                } else {
                    item.setStatus("FAILED");
                }

            } catch (Exception e) {
                item.setStatus("FAILED");
            }
        }

        itemRepo.saveAll(items);
    }

    public boolean simulateExternalCall() {
        // 80% 成功，20% 失敗
        return random.nextInt(10) < 8;
    }

    public void finalizeBatch(SettlementBatch batch) {

        List<SettlementItem> allItems =
                itemRepo.findAll();

        BigDecimal total = allItems.stream()
                .filter(i -> "SUCCESS".equals(i.getStatus()))
                .map(SettlementItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long count = allItems.stream()
                .filter(i -> "SUCCESS".equals(i.getStatus()))
                .count();

        batch.setTotalAmount(total);
        batch.setTotalCount((int) count);
        batch.setStatus("COMPLETED");
        batch.setCompletedAt(LocalDateTime.now());

        batchRepo.save(batch);
    }

}
