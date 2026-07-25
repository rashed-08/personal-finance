package io.rashed.finance.domain.recurring;

import io.rashed.finance.common.enums.RecurringExecutionStatus;
import io.rashed.finance.domain.transactions.TransactionId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * One row per due occurrence a scheduler run ever looked at, whether it
 * produced a transaction or not. Immutable, append-only log — see
 * docs/database/tables/recurring_transactions.md.
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class RecurringTransactionExecution {

    private final RecurringTransactionExecutionId id;

    private final RecurringTransactionId recurringTransactionId;

    private final LocalDate scheduledDate;

    private final RecurringExecutionStatus status;

    /** Set only when status is GENERATED. */
    private final TransactionId transactionId;

    /** Set only when status is SKIPPED. */
    private final String reason;

    private final LocalDateTime createdAt;

    public RecurringTransactionExecution(
            RecurringTransactionExecutionId id,
            RecurringTransactionId recurringTransactionId,
            LocalDate scheduledDate,
            RecurringExecutionStatus status,
            TransactionId transactionId,
            String reason,
            LocalDateTime createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.recurringTransactionId = Objects.requireNonNull(recurringTransactionId);
        this.scheduledDate = Objects.requireNonNull(scheduledDate);
        this.status = Objects.requireNonNull(status);
        this.transactionId = transactionId;
        this.reason = reason;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static RecurringTransactionExecution generated(
            RecurringTransactionId recurringTransactionId,
            LocalDate scheduledDate,
            TransactionId transactionId
    ) {

        Objects.requireNonNull(transactionId, "Generated execution requires a transaction.");

        return new RecurringTransactionExecution(
                RecurringTransactionExecutionId.newId(),
                recurringTransactionId,
                scheduledDate,
                RecurringExecutionStatus.GENERATED,
                transactionId,
                null,
                LocalDateTime.now()
        );
    }

    public static RecurringTransactionExecution skipped(
            RecurringTransactionId recurringTransactionId,
            LocalDate scheduledDate,
            String reason
    ) {

        Objects.requireNonNull(reason, "Skipped execution requires a reason.");

        return new RecurringTransactionExecution(
                RecurringTransactionExecutionId.newId(),
                recurringTransactionId,
                scheduledDate,
                RecurringExecutionStatus.SKIPPED,
                null,
                reason,
                LocalDateTime.now()
        );
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    public boolean isGenerated() {
        return status == RecurringExecutionStatus.GENERATED;
    }

    public boolean isSkipped() {
        return status == RecurringExecutionStatus.SKIPPED;
    }
}
