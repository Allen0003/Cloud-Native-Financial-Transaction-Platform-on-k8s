package com.transaction;

import com.transaction.batch.SettlementJob;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@SpringBootApplication
@RestController
@EnableJpaRepositories(basePackages = "com.transaction.domain.repository")
@EntityScan(basePackages = "com.transaction.domain.entity")
public class FinancialTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialTransactionApplication.class, args);
    }



    // 測試 mongo db 的 sharding, 因為已經在程式mod 做分工, 不需要去擋一次只有一個pod 可以做事
    @Bean
    CommandLineRunner testRun(SettlementJob job) {
        return args -> {
            job.runManual();
        };
    }


    //init 時做批次   只會有 1 個 pod 跑這一段
//    @Bean
//    CommandLineRunner testRun(SettlementJob job, LockingTaskExecutor lockExecutor) {
//        return args -> {
//            // 使用手動鎖：確保多個 Pod 啟動時，只有一個能跑這段 init 邏輯
//            lockExecutor.executeWithLock(
//                    (Runnable) job::runManual,
//                    new LockConfiguration(
//                            Instant.now(),
//                            "startup_settlement_lock", // 鎖的名稱，對應 shedlock 表
//                            Duration.ofMinutes(5),     // 最大鎖定時間（防止當機不釋放）
//                            Duration.ofSeconds(10)     // 最小鎖定時間（確保不會剛放掉就被另一個 Pod 撿走）
//                    )
//            );
//        };
//    }
}

