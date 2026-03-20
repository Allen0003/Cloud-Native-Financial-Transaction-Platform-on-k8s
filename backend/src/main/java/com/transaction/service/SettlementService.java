package com.transaction.service;

import com.transaction.domain.entity.SettlementBatch;

import java.time.LocalDate;

public interface SettlementService {
    void runBatch(LocalDate date);

    void processItems(SettlementBatch batch);

    boolean simulateExternalCall();


}
