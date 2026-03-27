package com.transaction.batch;

import com.transaction.service.SettlementKafkaService;
import com.transaction.service.SettlementMongoService;
import com.transaction.service.SettlementService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

//batch
@Component
public class SettlementJob {

    @Autowired
    SettlementService settlementService;

    @Autowired
    SettlementKafkaService settlementKafkaService;

    @Autowired
    SettlementMongoService settlementMongoService;


    public void runManual() {
        settlementMongoService.batchProcessLevelSettlement();
    }

//    @SchedulerLock(name = "daily_settlement_job_lock", lockAtLeastFor = "1m", lockAtMostFor = "10m")
//    public void runManual() {
//        settlementKafkaService.runBatch(LocalDate.now());
//    }

//    @Scheduled(cron = "0 0 2 * * ?")
//    public void run() {
//        settlementService.runBatch(LocalDate.now().minusDays(1));
//    }
}
