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

    @Transactional
    public void runBatch(LocalDate date) {
        // 1. 確保基礎資料存在 (使用 Enum)
        SettlementBatch batch = ensureBatchExists(date);

        // 2. 嘗試搶鎖 (原子操作：只有一個 Pod 能把 PENDING 改成 PROCESSING)
        int updated = batchRepo.tryLockBatch(date);

        if (updated == 0) {
            log.info("others is using ~~~~~~~~~~~~~~~~~~~~~~~~~");
            return;
        }
        log.info("I got lock  ~~~~~~~~~~~~~~~~~~~~~~~~~");

        // 3. 執行業務邏輯
        try {
            // 重新抓取最新狀態實體
            SettlementBatch currentBatch = batchRepo.findByBatchDate(date).orElseThrow();

            // 執行 idempotent 插入
            itemRepo.insertSettlementItems(currentBatch.getId());

            // TODO: 其他處理邏輯...

            // 4. 更新為完成 (使用 Enum)
            currentBatch.setStatus(SettlementStatus.COMPLETED.getCode());
            currentBatch.setCompletedAt(LocalDateTime.now());
            batchRepo.save(currentBatch);

        } catch (Exception e) {
            // 如果失敗，標記為 FAILED，以便後續人工排查或重試
            batchRepo.findByBatchDate(date).ifPresent(b -> {
                b.setStatus(SettlementStatus.FAILED.getCode());
                batchRepo.save(b);
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
            try {// 模擬外部銀行 API
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
