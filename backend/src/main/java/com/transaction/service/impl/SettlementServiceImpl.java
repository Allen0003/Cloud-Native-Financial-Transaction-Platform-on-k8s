package com.transaction.service.impl;

import com.transaction.domain.entity.SettlementBatch;
import com.transaction.domain.entity.SettlementItem;
import com.transaction.domain.enums.SettlementStatus;
import com.transaction.domain.repository.SettlementBatchRepository;
import com.transaction.domain.repository.SettlementItemRepository;
import com.transaction.service.SettlementService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class SettlementServiceImpl implements SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementServiceImpl.class);
    @Autowired
    SettlementBatchRepository batchRepo;

    @Autowired
    SettlementItemRepository itemRepo;

    private final Random random = new Random();


    public void runBatch(LocalDate date) {
        // 1. 確保基礎資料存在 (使用 Enum)
        ensureBatchExists(date);

        // 2. 嘗試搶鎖 (原子操作：只有一個 Pod 能把 PENDING 改成 PROCESSING)
        // 第一天第一次 不會更新 start_at
        int locked = batchRepo.tryLockBatchWithTimeout(date, LocalDateTime.now().minusMinutes(1));

        if (locked == 0) {
            return;
        }
        log.info("I got lock  ~~~~~~~~~~~~~~~~~~~~~~~~~");

        // 3. 執行業務邏輯
        try {
            SettlementBatch currentBatch = batchRepo.findByBatchDate(date).orElseThrow();

            // 執行 idempotent 插入
            itemRepo.insertSettlementItems(currentBatch.getId());

            processItems(currentBatch);

            // 4. 更新為完成 (使用 Enum)
            currentBatch.setStatus(SettlementStatus.COMPLETED.getCode());
            currentBatch.setCompletedAt(LocalDateTime.now());
            batchRepo.save(currentBatch);

        } catch (Exception e) {
            // 如果失敗，標記為 FAILED，以便後續人工排查或重試
            batchRepo.findByBatchDate(date).ifPresent(b -> {
                log.error("Batch crashed", e);
                throw e; // 讓 K8s restart pod
            });
            throw e;
        }
    }

    private SettlementBatch ensureBatchExists(LocalDate date) {
        try {
            return batchRepo.findByBatchDate(date).orElseGet(() -> {
                SettlementBatch b = new SettlementBatch();
                b.setBatchDate(date);
                b.setStatus(SettlementStatus.PENDING.getCode());
                return batchRepo.saveAndFlush(b); // 強制寫入
            });
        } catch (DataIntegrityViolationException e) {
            // 如果發生唯一鍵衝突，表示別的 Pod 搶先建好了，我們直接抓現成的
            return batchRepo.findByBatchDate(date).orElseThrow();
        }
    }


    public void processItems(SettlementBatch batch) {
        List<SettlementItem> items = itemRepo.findPendingItems(batch.getId());
        for (SettlementItem item : items) {

            // 🔥 隨機 crash（10% 機率）
            if (random.nextInt(10) == 0) {
                log.error("💥 Simulating batch crash!");
                throw new RuntimeException("Simulated crash");
            }

            try {
                processSingleItem(item); // 每筆獨立 transaction
            } catch (Exception e) {
                log.error("process item failed: {}", item.getId(), e);
            }
        }
    }

    @Transactional
    public void processSingleItem(SettlementItem item) {

        boolean success = simulateExternalCall();

        if (success) {
            item.setStatus(SettlementStatus.SUCCESS.getCode());
        } else {
            item.setStatus(SettlementStatus.FAILED.getCode());
        }

        itemRepo.save(item);
    }


    public boolean simulateExternalCall() {
        // 80% 成功，20% 失敗
        return random.nextInt(10) < 8;
    }

    public void finalizeBatch(SettlementBatch batch) {
        List<SettlementItem> allItems = itemRepo.findAll();

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
