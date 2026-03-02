package com.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
// 1. 強制讓 Spring 去找你的 Repository 介面
@EnableJpaRepositories(basePackages = "com.transaction.domain.repository")
// 2. 強制讓 Spring 去找你的 Entity (資料表對應類)
@EntityScan(basePackages = "com.transaction.domain.entity")
public class FinancialTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialTransactionApplication.class, args);
    }
}

