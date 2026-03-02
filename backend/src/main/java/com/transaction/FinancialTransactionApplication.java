package com.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class FinancialTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialTransactionApplication.class, args);
    }

}
