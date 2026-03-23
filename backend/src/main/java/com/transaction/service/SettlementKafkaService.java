package com.transaction.service;

import java.time.LocalDate;

public interface SettlementKafkaService {
    void runBatch(LocalDate date);

}
