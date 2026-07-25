package io.rashed.finance.domain.reconciliation;

import io.rashed.finance.common.enums.ReconciliationStatus;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.transactions.TransactionId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root for a cash reconciliation session. Compares the account's
 * ledger-derived balance ("expected") against one or more physical cash
 * counts ("snapshots"), then finalizes with an adjustment transaction if
 * they differ. See docs/business/CashReconciliationWorkflow.md.
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class CashReconciliation {

    private final CashReconciliationId id;

    private final AccountId accountId;

    private final LocalDate reconciliationDate;

    /**
     * Derived from the ledger at the moment the session starts. Never
     * entered manually.
     */
    private final Money expectedCashAmount;

    /**
     * The most recently recorded snapshot's amount. Null until at least
     * one snapshot has been recorded.
     */
    private final Money actualCashAmount;

    /**
     * actualCashAmount - expectedCashAmount. Null under the same condition
     * as actualCashAmount.
     */
    private final Money difference;

    private final ReconciliationStatus status;

    /**
     * Set once a non-zero difference has been resolved by an adjustment
     * transaction. Remains null for a perfectly balanced reconciliation.
     */
    private final TransactionId adjustmentTransactionId;

    private final String notes;

    private final List<CashSnapshot> snapshots;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public CashReconciliation(
            CashReconciliationId id,
            AccountId accountId,
            LocalDate reconciliationDate,
            Money expectedCashAmount,
            Money actualCashAmount,
            Money difference,
            ReconciliationStatus status,
            TransactionId adjustmentTransactionId,
            String notes,
            List<CashSnapshot> snapshots,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        this.id = Objects.requireNonNull(id);
        this.accountId = Objects.requireNonNull(accountId);
        this.reconciliationDate = Objects.requireNonNull(reconciliationDate);
        this.expectedCashAmount = Objects.requireNonNull(expectedCashAmount, "Expected cash amount cannot be null.");
        this.status = Objects.requireNonNull(status);

        if (status == ReconciliationStatus.COMPLETED && (actualCashAmount == null || difference == null)) {
            throw new IllegalArgumentException(
                    "A completed reconciliation must have an actual cash amount and a difference.");
        }

        this.actualCashAmount = actualCashAmount;
        this.difference = difference;
        this.adjustmentTransactionId = adjustmentTransactionId;
        this.notes = notes;
        this.snapshots = List.copyOf(Objects.requireNonNull(snapshots));

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static CashReconciliation start(
            AccountId accountId,
            LocalDate reconciliationDate,
            Money expectedCashAmount,
            String notes
    ) {

        Objects.requireNonNull(accountId, "Account cannot be null.");
        Objects.requireNonNull(reconciliationDate, "Reconciliation date cannot be null.");
        Objects.requireNonNull(expectedCashAmount, "Expected cash amount cannot be null.");

        if (expectedCashAmount.isNegative()) {
            throw new IllegalArgumentException("Expected cash amount cannot be negative.");
        }

        LocalDateTime now = LocalDateTime.now();

        return new CashReconciliation(
                CashReconciliationId.newId(),
                accountId,
                reconciliationDate,
                expectedCashAmount,
                null,
                null,
                ReconciliationStatus.PENDING,
                null,
                notes,
                List.of(),
                now,
                now
        );
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    public boolean isPending() {
        return status == ReconciliationStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == ReconciliationStatus.COMPLETED;
    }

    public boolean hasSnapshots() {
        return !snapshots.isEmpty();
    }

    public boolean isBalanced() {
        return difference != null && difference.isZero();
    }

    public boolean requiresAdjustment() {
        return difference != null && !difference.isZero();
    }

    /** Records a physical cash count. The latest count becomes the current actual amount. */
    public CashReconciliation addSnapshot(Money cashAmount, String snapshotNotes) {

        if (isCompleted()) {
            throw new IllegalStateException("Cannot record a cash count on a completed reconciliation.");
        }

        CashSnapshot snapshot = CashSnapshot.record(cashAmount, snapshotNotes);

        List<CashSnapshot> updatedSnapshots = new ArrayList<>(snapshots);
        updatedSnapshots.add(snapshot);

        Money newDifference = cashAmount.subtract(expectedCashAmount);

        return new CashReconciliation(
                id, accountId, reconciliationDate, expectedCashAmount,
                cashAmount, newDifference, status, adjustmentTransactionId, notes,
                updatedSnapshots, createdAt, LocalDateTime.now()
        );
    }

    /** Finalizes the session using the most recently recorded snapshot. */
    public CashReconciliation complete() {

        if (isCompleted()) {
            throw new IllegalStateException("Reconciliation is already completed.");
        }

        if (!hasSnapshots()) {
            throw new IllegalStateException("At least one cash count is required before completing a reconciliation.");
        }

        return new CashReconciliation(
                id, accountId, reconciliationDate, expectedCashAmount,
                actualCashAmount, difference, ReconciliationStatus.COMPLETED, adjustmentTransactionId, notes,
                snapshots, createdAt, LocalDateTime.now()
        );
    }

    /** Attaches the adjustment transaction created to resolve a non-zero difference. */
    public CashReconciliation linkAdjustment(TransactionId transactionId) {

        Objects.requireNonNull(transactionId, "Adjustment transaction id cannot be null.");

        if (adjustmentTransactionId != null) {
            throw new IllegalStateException("Reconciliation already has a linked adjustment transaction.");
        }

        return new CashReconciliation(
                id, accountId, reconciliationDate, expectedCashAmount,
                actualCashAmount, difference, status, transactionId, notes,
                snapshots, createdAt, LocalDateTime.now()
        );
    }
}
