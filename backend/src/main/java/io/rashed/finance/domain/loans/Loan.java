package io.rashed.finance.domain.loans;

import io.rashed.finance.common.enums.LoanStatus;
import io.rashed.finance.common.enums.LoanType;
import io.rashed.finance.common.valueobject.Money;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class Loan {

    private final LoanId id;

    /**
     * Person or organization.
     */
    private final String name;

    /**
     * RECEIVABLE / PAYABLE
     */
    private final LoanType loanType;

    /**
     * Original principal amount.
     */
    private final Money principalAmount;

    /**
     * Date the loan started (disbursed/received).
     */
    private final LocalDate startDate;

    /**
     * Optional target settlement date.
     */
    private final LocalDate dueDate;

    /**
     * ACTIVE / CLOSED / CANCELLED
     */
    private final LoanStatus loanStatus;

    /**
     * Optional note.
     */
    private final String description;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public Loan(
            LoanId id,
            String name,
            LoanType loanType,
            Money principalAmount,
            LocalDate startDate,
            LocalDate dueDate,
            LoanStatus loanStatus,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name).trim();
        this.loanType = Objects.requireNonNull(loanType);
        this.principalAmount = Objects.requireNonNull(principalAmount);
        this.startDate = Objects.requireNonNull(startDate);
        this.dueDate = dueDate;
        this.loanStatus = Objects.requireNonNull(loanStatus);
        this.description = description;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static Loan receivable(
            String name,
            Money principalAmount,
            LocalDate startDate,
            LocalDate dueDate,
            String description
    ) {

        validateName(name);
        validatePrincipalAmount(principalAmount);
        validateDueDate(dueDate);

        LocalDateTime now = LocalDateTime.now();

        return new Loan(
                LoanId.newId(),
                name,
                LoanType.RECEIVABLE,
                principalAmount,
                startDate,
                dueDate,
                LoanStatus.ACTIVE,
                description,
                now,
                now
        );
    }

    public static Loan payable(
            String name,
            Money principalAmount,
            LocalDate startDate,
            LocalDate dueDate,
            String description
    ) {

        validateName(name);
        validatePrincipalAmount(principalAmount);
        validateDueDate(dueDate);

        LocalDateTime now = LocalDateTime.now();

        return new Loan(
                LoanId.newId(),
                name,
                LoanType.PAYABLE,
                principalAmount,
                startDate,
                dueDate,
                LoanStatus.ACTIVE,
                description,
                now,
                now
        );
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void validateName(String name) {

        Objects.requireNonNull(name, "Loan name cannot be null.");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Loan name cannot be empty."
            );
        }
    }

    private static void validatePrincipalAmount(Money amount) {

        Objects.requireNonNull(
                amount,
                "Principal amount cannot be null."
        );

        if (!amount.isPositive()) {
            throw new IllegalArgumentException(
                    "Principal amount must be greater than zero."
            );
        }
    }

    private static void validateDueDate(LocalDate dueDate) {

        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Due date cannot be in the past."
            );
        }
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    public boolean isReceivable() {
        return loanType == LoanType.RECEIVABLE;
    }

    public boolean isPayable() {
        return loanType == LoanType.PAYABLE;
    }

    public boolean isActive() {
        return loanStatus == LoanStatus.ACTIVE;
    }

    public boolean isClosed() {
        return loanStatus == LoanStatus.CLOSED;
    }

    public boolean isCancelled() {
        return loanStatus == LoanStatus.CANCELLED;
    }

    public boolean hasDueDate() {
        return dueDate != null;
    }

    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    public Loan rename(String name) {

        validateName(name);

        return new Loan(
                id,
                name,
                loanType,
                principalAmount,
                startDate,
                dueDate,
                loanStatus,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Loan changeDescription(String description) {

        return new Loan(
                id,
                name,
                loanType,
                principalAmount,
                startDate,
                dueDate,
                loanStatus,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Loan changeDueDate(LocalDate dueDate) {

        validateDueDate(dueDate);

        return new Loan(
                id,
                name,
                loanType,
                principalAmount,
                startDate,
                dueDate,
                loanStatus,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    /** A loan may only be closed once its outstanding balance is zero. */
    public Loan close() {

        if (isClosed()) {
            return this;
        }

        if (isCancelled()) {
            throw new IllegalStateException(
                    "Cancelled loan cannot be closed."
            );
        }

        return new Loan(
                id,
                name,
                loanType,
                principalAmount,
                startDate,
                dueDate,
                LoanStatus.CLOSED,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Loan cancel() {

        if (isCancelled()) {
            return this;
        }

        if (isClosed()) {
            throw new IllegalStateException(
                    "Closed loan cannot be cancelled."
            );
        }

        return new Loan(
                id,
                name,
                loanType,
                principalAmount,
                startDate,
                dueDate,
                LoanStatus.CANCELLED,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

}
