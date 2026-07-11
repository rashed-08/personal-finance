package io.rashed.finance.domain.reconciliation;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class Reconciliation {

    private final ReconciliationId id;

    /**
     * Cash account being reconciled.
     */
    private final AccountId accountId;

    /**
     * Reconciliation date.
     */
    private final LocalDate reconciliationDate;

    /**
     * Balance calculated from ledger.
     */
    private final Money expectedBalance;

    /**
     * Balance counted physically.
     */
    private final Money actualBalance;

    /**
     * actualBalance - expectedBalance
     */
    private final Money difference;

    /**
     * Optional explanation.
     */
    private final String description;

    /**
     * Whether reconciliation has been resolved
     * by an adjustment transaction.
     */
    private final boolean resolved;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public Reconciliation(
            ReconciliationId id,
            AccountId accountId,
            LocalDate reconciliationDate,
            Money expectedBalance,
            Money actualBalance,
            Money difference,
            String description,
            boolean resolved,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        this.id = Objects.requireNonNull(id);

        this.accountId = Objects.requireNonNull(accountId);

        this.reconciliationDate =
                Objects.requireNonNull(reconciliationDate);

        this.expectedBalance =
                Objects.requireNonNull(expectedBalance);

        this.actualBalance =
                Objects.requireNonNull(actualBalance);

        this.difference =
                Objects.requireNonNull(difference);

        this.description = description;

        this.resolved = resolved;

        this.createdAt =
                Objects.requireNonNull(createdAt);

        this.updatedAt =
                Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static Reconciliation create(
            AccountId accountId,
            LocalDate reconciliationDate,
            Money expectedBalance,
            Money actualBalance,
            String description
    ) {

        validateAccount(accountId);
        validateReconciliationDate(reconciliationDate);
        validateBalances(expectedBalance, actualBalance);
        validateDescription(description);

        LocalDateTime now = LocalDateTime.now();

        Money difference =
                actualBalance.subtract(expectedBalance);

        return new Reconciliation(
                ReconciliationId.newId(),
                accountId,
                reconciliationDate,
                expectedBalance,
                actualBalance,
                difference,
                description,
                false,
                now,
                now
        );
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void validateAccount(AccountId accountId) {

        Objects.requireNonNull(
                accountId,
                "Account cannot be null."
        );
    }

    private static void validateBalances(
            Money expectedBalance,
            Money actualBalance
    ) {

        Objects.requireNonNull(
                expectedBalance,
                "Expected balance cannot be null."
        );

        Objects.requireNonNull(
                actualBalance,
                "Actual balance cannot be null."
        );

        if (expectedBalance.isNegative()) {
            throw new IllegalArgumentException(
                    "Expected balance cannot be negative."
            );
        }

        if (actualBalance.isNegative()) {
            throw new IllegalArgumentException(
                    "Actual balance cannot be negative."
            );
        }
    }

    private static void validateDescription(String description) {

        if (description != null && description.length() > 1000) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 1000 characters."
            );
        }
    }

    private static void validateReconciliationDate(
        LocalDate reconciliationDate
) {

    Objects.requireNonNull(
            reconciliationDate,
            "Reconciliation date cannot be null."
    );
}

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    public boolean isResolved() {
        return resolved;
    }

    public boolean isBalanced() {
        return difference.isZero();
    }

    public boolean hasDifference() {
        return !difference.isZero();
    }

    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    public Reconciliation resolve() {

        if (resolved) {
            return this;
        }

        return new Reconciliation(
                id,
                accountId,
                reconciliationDate,
                expectedBalance,
                actualBalance,
                difference,
                description,
                true,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Reconciliation reopen() {

        if (!resolved) {
            return this;
        }

        return new Reconciliation(
                id,
                accountId,
                reconciliationDate,
                expectedBalance,
                actualBalance,
                difference,
                description,
                false,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Reconciliation changeDescription(String description) {

        validateDescription(description);

        return new Reconciliation(
                id,
                accountId,
                reconciliationDate,
                expectedBalance,
                actualBalance,
                difference,
                description,
                resolved,
                createdAt,
                LocalDateTime.now()
        );
    }
}