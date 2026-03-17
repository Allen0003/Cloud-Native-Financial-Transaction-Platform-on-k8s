package com.transaction.domain.entity;


import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlement_batch",
        uniqueConstraints = @UniqueConstraint(columnNames = "batch_date"))

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_date", nullable = false)
    private LocalDate batchDate;

    @Column(nullable = false)
    private String status; // PENDING / PROCESSING / COMPLETED / FAILED

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    private Integer version;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(LocalDate batchDate) {
        this.batchDate = batchDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
