package io.rashed.finance.infrastructure.persistence.entity;

import io.rashed.finance.common.enums.RecurringExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recurring_transaction_executions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecurringTransactionExecutionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "recurring_transaction_id", nullable = false, updatable = false)
    private UUID recurringTransactionId;

    @Column(name = "scheduled_date", nullable = false, updatable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private RecurringExecutionStatus status;

    @Column(name = "transaction_id", updatable = false)
    private UUID transactionId;

    @Column(length = 500, updatable = false)
    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RecurringTransactionExecutionEntity(
            UUID id,
            UUID recurringTransactionId,
            LocalDate scheduledDate,
            RecurringExecutionStatus status,
            UUID transactionId,
            String reason,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.recurringTransactionId = recurringTransactionId;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.transactionId = transactionId;
        this.reason = reason;
        this.createdAt = createdAt;
    }
}
