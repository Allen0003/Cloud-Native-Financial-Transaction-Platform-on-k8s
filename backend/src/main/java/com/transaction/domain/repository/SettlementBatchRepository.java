package com.transaction.domain.repository;


import com.transaction.domain.entity.SettlementBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, Long> {

    Optional<SettlementBatch> findByBatchDate(LocalDate batchDate);

    @Modifying
    @Transactional
    @Query("""
                UPDATE SettlementBatch b 
                SET b.status = 'PROCESSING'
                WHERE b.batchDate = :date AND b.status = 'PENDING'
            """)
    int tryLockBatch(@Param("date") LocalDate date);
}
