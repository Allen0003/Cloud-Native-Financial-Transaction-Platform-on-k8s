package com.transaction.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_item",
        uniqueConstraints = @UniqueConstraint(columnNames = "transaction_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status; // PENDING / SUCCESS / FAILED

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
