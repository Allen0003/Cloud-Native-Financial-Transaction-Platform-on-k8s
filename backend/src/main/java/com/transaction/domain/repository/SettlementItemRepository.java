package com.transaction.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.transaction.domain.entity.SettlementItem;


@Repository
public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO settlement_item (batch_id, transaction_id, amount, status)
        SELECT :batchId, t.transaction_id, t.amount, 'PENDING'
        FROM transactions t
        WHERE t.status = 'SUCCESS'
        ON DUPLICATE KEY UPDATE transaction_id = transaction_id
    """, nativeQuery = true)
    int insertSettlementItems(@Param("batchId") Long batchId);
}
