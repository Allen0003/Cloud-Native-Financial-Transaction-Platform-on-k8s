package com.transaction.service;

import java.time.LocalDate;

public interface SettlementService {
    void runBatch(LocalDate date);
}
