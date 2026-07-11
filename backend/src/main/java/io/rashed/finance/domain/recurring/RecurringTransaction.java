package io.rashed.finance.domain.recurring;

import io.rashed.finance.common.enums.Frequency;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class RecurringTransaction {

    private final RecurringTransactionId id;

    /**
     * Expense / Income
     */
    private final TransactionType transactionType;

    /**
     * Monthly / Weekly / Yearly...
     */
    private final Frequency frequency;

    /**
     * Amount for each occurrence.
     */
    private final Money amount;

    /**
     * Source account (expense).
     */
    private final AccountId fromAccountId;

    /**
     * Destination account (income).
     */
    private final AccountId toAccountId;

    /**
     * Expense/Income category.
     */
    private final CategoryId categoryId;

    /**
     * First execution date.
     */
    private final LocalDate startDate;

    /**
     * Optional end date.
     */
    private final LocalDate endDate;

    /**
     * Whether scheduler should execute it.
     */
    private final boolean active;

    /**
     * Optional description.
     */
    private final String description;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public RecurringTransaction(
            RecurringTransactionId id,
            TransactionType transactionType,
            Frequency frequency,
            Money amount,
            AccountId fromAccountId,
            AccountId toAccountId,
            CategoryId categoryId,
            LocalDate startDate,
            LocalDate endDate,
            boolean active,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        this.id = Objects.requireNonNull(id);
        this.transactionType = Objects.requireNonNull(transactionType);
        this.frequency = Objects.requireNonNull(frequency);
        this.amount = Objects.requireNonNull(amount);

        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.categoryId = categoryId;

        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = endDate;

        this.active = active;
        this.description = description == null ? null : description.trim();

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static RecurringTransaction expense(
            AccountId fromAccountId,
            CategoryId categoryId,
            Money amount,
            Frequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            String description
    ) {

        validateAmount(amount);
        validateExpense(fromAccountId, categoryId);
        validateFrequency(frequency);
        validateStartDate(startDate);
        validateDateRange(startDate, endDate);

        LocalDateTime now = LocalDateTime.now();

        return new RecurringTransaction(
                RecurringTransactionId.newId(),
                TransactionType.EXPENSE,
                frequency,
                amount,
                fromAccountId,
                null,
                categoryId,
                startDate,
                endDate,
                true,
                description,
                now,
                now
        );
    }

    public static RecurringTransaction income(
            AccountId toAccountId,
            CategoryId categoryId,
            Money amount,
            Frequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            String description
    ) {

        validateAmount(amount);
        validateIncome(toAccountId, categoryId);
        validateFrequency(frequency);
        validateStartDate(startDate);
        validateDateRange(startDate, endDate);

        LocalDateTime now = LocalDateTime.now();

        return new RecurringTransaction(
                RecurringTransactionId.newId(),
                TransactionType.INCOME,
                frequency,
                amount,
                null,
                toAccountId,
                categoryId,
                startDate,
                endDate,
                true,
                description,
                now,
                now
        );
    }

    

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void validateAmount(Money amount) {

        Objects.requireNonNull(amount, "Amount cannot be null.");

        if (!amount.isPositive()) {
            throw new IllegalArgumentException(
                    "Recurring amount must be greater than zero."
            );
        }
    }

    private static void validateExpense(AccountId fromAccountId,
                                        CategoryId categoryId) {

        Objects.requireNonNull(
                fromAccountId,
                "Expense requires a source account."
        );

        Objects.requireNonNull(
                categoryId,
                "Expense requires a category."
        );
    }

    private static void validateIncome(AccountId toAccountId,
                                       CategoryId categoryId) {

        Objects.requireNonNull(
                toAccountId,
                "Income requires a destination account."
        );

        Objects.requireNonNull(
                categoryId,
                "Income requires a category."
        );
    }

    private static void validateFrequency(Frequency frequency) {

        Objects.requireNonNull(
                frequency,
                "Frequency cannot be null."
        );
    }

    private static void validateStartDate(LocalDate startDate) {

        Objects.requireNonNull(
                startDate,
                "Start date cannot be null."
        );
    }

    private static void validateDateRange(LocalDate startDate,
                                          LocalDate endDate) {

        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date."
            );
        }
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    public boolean isExpense() {
        return transactionType == TransactionType.EXPENSE;
    }

    public boolean isIncome() {
        return transactionType == TransactionType.INCOME;
    }

    public boolean isActive() {
        return active;
    }

    public boolean hasEndDate() {
        return endDate != null;
    }

    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    public RecurringTransaction activate() {

        if (active) {
            return this;
        }

        return new RecurringTransaction(
                id,
                transactionType,
                frequency,
                amount,
                fromAccountId,
                toAccountId,
                categoryId,
                startDate,
                endDate,
                true,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public RecurringTransaction deactivate() {

        if (!active) {
            return this;
        }

        return new RecurringTransaction(
                id,
                transactionType,
                frequency,
                amount,
                fromAccountId,
                toAccountId,
                categoryId,
                startDate,
                endDate,
                false,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public RecurringTransaction changeAmount(Money amount) {

        validateAmount(amount);

        return new RecurringTransaction(
                id,
                transactionType,
                frequency,
                amount,
                fromAccountId,
                toAccountId,
                categoryId,
                startDate,
                endDate,
                active,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public RecurringTransaction changeFrequency(Frequency frequency) {

        validateFrequency(frequency);

        return new RecurringTransaction(
                id,
                transactionType,
                frequency,
                amount,
                fromAccountId,
                toAccountId,
                categoryId,
                startDate,
                endDate,
                active,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public RecurringTransaction changeDateRange(LocalDate startDate,
                                                LocalDate endDate) {

        validateStartDate(startDate);
        validateDateRange(startDate, endDate);

        return new RecurringTransaction(
                id,
                transactionType,
                frequency,
                amount,
                fromAccountId,
                toAccountId,
                categoryId,
                startDate,
                endDate,
                active,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }

    public RecurringTransaction changeDescription(String description) {

        return new RecurringTransaction(
                id,
                transactionType,
                frequency,
                amount,
                fromAccountId,
                toAccountId,
                categoryId,
                startDate,
                endDate,
                active,
                description,
                createdAt,
                LocalDateTime.now()
        );
    }
}