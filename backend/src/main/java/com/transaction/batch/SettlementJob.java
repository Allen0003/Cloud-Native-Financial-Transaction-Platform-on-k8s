package com.transaction.batch;

import com.transaction.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

//batch
@Component
public class SettlementJob {

    private final SettlementService settlementService;

    public SettlementJob(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    public void runManual() {
        settlementService.runBatch(LocalDate.now());
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void run() {
        settlementService.runBatch(LocalDate.now().minusDays(1));
    }
}
