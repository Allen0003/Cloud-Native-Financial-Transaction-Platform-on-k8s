package com.transaction;

import com.transaction.batch.SettlementJob;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableJpaRepositories(basePackages = "com.transaction.domain.repository")
@EntityScan(basePackages = "com.transaction.domain.entity")
public class FinancialTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialTransactionApplication.class, args);
    }

    @Bean
    CommandLineRunner testRun(SettlementJob job) {
        return args -> {
            job.runManual();
        };
    }

}

