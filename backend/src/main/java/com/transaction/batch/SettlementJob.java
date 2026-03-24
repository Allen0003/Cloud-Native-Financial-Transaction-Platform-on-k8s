package com.transaction.batch;

import com.transaction.service.SettlementKafkaService;
import com.transaction.service.SettlementService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

//batch
@Component
public class SettlementJob {

    @Autowired
    SettlementService settlementService;

    @Autowired
    SettlementKafkaService settlementKafkaService;

    @SchedulerLock(name = "daily_settlement_job_lock", lockAtLeastFor = "1m", lockAtMostFor = "10m")
    public void runManual() {
        settlementKafkaService.runBatch(LocalDate.now());
    }

//    @Scheduled(cron = "0 0 2 * * ?")
//    public void run() {
//        settlementService.runBatch(LocalDate.now().minusDays(1));
//    }
}
