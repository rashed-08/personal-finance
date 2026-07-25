package io.rashed.finance.infrastructure.persistence.entity;

import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus transactionStatus;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column
    private UUID fromAccountId;

    @Column
    private UUID toAccountId;

    @Column
    private UUID categoryId;

    @Column
    private UUID salaryCycleId;

    @Column(length = 100)
    private String referenceNumber;

    @Column(length = 100)
    private String migrationBatchId;

    @Column(length = 100)
    private String reconciliationBatchId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private AdjustmentReason adjustmentReason;

    @Column(length = 255)
    private String description;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name="reference_transaction_id")
    private UUID referenceTransactionId;

    public TransactionEntity(
            UUID id,
            TransactionType transactionType,
            TransactionStatus transactionStatus,
            LocalDate transactionDate,
            BigDecimal amount,
            UUID fromAccountId,
            UUID toAccountId,
            UUID categoryId,
            UUID salaryCycleId,
            String referenceNumber,
            String migrationBatchId,
            String reconciliationBatchId,
            AdjustmentReason adjustmentReason,
            String description,
            String notes,
            UUID referenceTransactionId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.categoryId = categoryId;
        this.salaryCycleId = salaryCycleId;
        this.referenceNumber = referenceNumber;
        this.migrationBatchId = migrationBatchId;
        this.reconciliationBatchId = reconciliationBatchId;
        this.adjustmentReason = adjustmentReason;
        this.description = description;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.referenceTransactionId = referenceTransactionId;
    }
}