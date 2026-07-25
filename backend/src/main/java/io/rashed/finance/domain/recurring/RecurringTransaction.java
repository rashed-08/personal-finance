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
     * Template name, e.g. "House Rent".
     */
    private final String name;

    /**
     * Expense / Income / Transfer
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
     * Source account (expense, transfer).
     */
    private final AccountId fromAccountId;

    /**
     * Destination account (income, transfer).
     */
    private final AccountId toAccountId;

    /**
     * Expense/Income category. Never set for TRANSFER.
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
     * The next date this template is due. Starts equal to startDate and
     * advances by frequency each time an occurrence is executed or skipped.
     */
    private final LocalDate nextExecutionDate;

    /**
     * The scheduled date of the most recent successfully generated
     * occurrence. Null until the first execution.
     */
    private final LocalDate lastExecutionDate;

    /**
     * Whether due occurrences generate a transaction automatically
     * (RunDueRecurringTransactionsService) or require the user to confirm
     * each one explicitly.
     */
    private final boolean autoGenerate;

    /**
     * Whether scheduler should consider it at all.
     */
    private final boolean active;

    /**
     * Optional description.
     */
    private final String description;

    /**
     * Optional notes.
     */
    private final String notes;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public RecurringTransaction(
            RecurringTransactionId id,
            String name,
            TransactionType transactionType,
            Frequency frequency,
            Money amount,
            AccountId fromAccountId,
            AccountId toAccountId,
            CategoryId categoryId,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate nextExecutionDate,
            LocalDate lastExecutionDate,
            boolean autoGenerate,
            boolean active,
            String description,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name).trim();
        this.transactionType = Objects.requireNonNull(transactionType);
        this.frequency = Objects.requireNonNull(frequency);
        this.amount = Objects.requireNonNull(amount);

        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.categoryId = categoryId;

        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = endDate;
        this.nextExecutionDate = Objects.requireNonNull(nextExecutionDate);
        this.lastExecutionDate = lastExecutionDate;

        this.autoGenerate = autoGenerate;
        this.active = active;
        this.description = description == null ? null : description.trim();
        this.notes = notes == null ? null : notes.trim();

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static RecurringTransaction expense(
            String name,
            AccountId fromAccountId,
            CategoryId categoryId,
            Money amount,
            Frequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            boolean autoGenerate,
            String description,
            String notes
    ) {

        validateName(name);
        validateAmount(amount);
        validateExpense(fromAccountId, categoryId);
        validateFrequency(frequency);
        validateStartDate(startDate);
        validateDateRange(startDate, endDate);

        LocalDateTime now = LocalDateTime.now();

        return new RecurringTransaction(
                RecurringTransactionId.newId(),
                name,
                TransactionType.EXPENSE,
                frequency,
                amount,
                fromAccountId,
                null,
                categoryId,
                startDate,
                endDate,
                startDate,
                null,
                autoGenerate,
                true,
                description,
                notes,
                now,
                now
        );
    }

    public static RecurringTransaction income(
            String name,
            AccountId toAccountId,
            CategoryId categoryId,
            Money amount,
            Frequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            boolean autoGenerate,
            String description,
            String notes
    ) {

        validateName(name);
        validateAmount(amount);
        validateIncome(toAccountId, categoryId);
        validateFrequency(frequency);
        validateStartDate(startDate);
        validateDateRange(startDate, endDate);

        LocalDateTime now = LocalDateTime.now();

        return new RecurringTransaction(
                RecurringTransactionId.newId(),
                name,
                TransactionType.INCOME,
                frequency,
                amount,
                null,
                toAccountId,
                categoryId,
                startDate,
                endDate,
                startDate,
                null,
                autoGenerate,
                true,
                description,
                notes,
                now,
                now
        );
    }

    public static RecurringTransaction transfer(
            String name,
            AccountId fromAccountId,
            AccountId toAccountId,
            Money amount,
            Frequency frequency,
            LocalDate startDate,
            LocalDate endDate,
            boolean autoGenerate,
            String description,
            String notes
    ) {

        validateName(name);
        validateAmount(amount);
        validateTransfer(fromAccountId, toAccountId);
        validateFrequency(frequency);
        validateStartDate(startDate);
        validateDateRange(startDate, endDate);

        LocalDateTime now = LocalDateTime.now();

        return new RecurringTransaction(
                RecurringTransactionId.newId(),
                name,
                TransactionType.TRANSFER,
                frequency,
                amount,
                fromAccountId,
                toAccountId,
                null,
                startDate,
                endDate,
                startDate,
                null,
                autoGenerate,
                true,
                description,
                notes,
                now,
                now
        );
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void validateName(String name) {

        Objects.requireNonNull(name, "Name cannot be null.");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException("Name cannot exceed 100 characters.");
        }
    }

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

    private static void validateTransfer(AccountId fromAccountId, AccountId toAccountId) {

        Objects.requireNonNull(fromAccountId, "Transfer requires a source account.");

        Objects.requireNonNull(toAccountId, "Transfer requires a destination account.");

        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same.");
        }
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

    public boolean isTransfer() {
        return transactionType == TransactionType.TRANSFER;
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

    public boolean hasLastExecutionDate() {
        return lastExecutionDate != null;
    }

    /** Whether nextExecutionDate has arrived (inclusive) as of the given date. */
    public boolean isDue(LocalDate asOfDate) {

        Objects.requireNonNull(asOfDate, "Date cannot be null.");

        if (!active) {
            return false;
        }

        if (hasEndDate() && nextExecutionDate.isAfter(endDate)) {
            return false;
        }

        return !nextExecutionDate.isAfter(asOfDate);
    }

    public RecurringTransaction activate() {

        if (active) {
            return this;
        }

        return new RecurringTransaction(
                id, name, transactionType, frequency, amount, fromAccountId, toAccountId, categoryId,
                startDate, endDate, nextExecutionDate, lastExecutionDate, autoGenerate, true,
                description, notes, createdAt, LocalDateTime.now()
        );
    }

    public RecurringTransaction deactivate() {

        if (!active) {
            return this;
        }

        return new RecurringTransaction(
                id, name, transactionType, frequency, amount, fromAccountId, toAccountId, categoryId,
                startDate, endDate, nextExecutionDate, lastExecutionDate, autoGenerate, false,
                description, notes, createdAt, LocalDateTime.now()
        );
    }

    public RecurringTransaction rename(String name) {

        validateName(name);

        return new RecurringTransaction(
                id, name, transactionType, frequency, amount, fromAccountId, toAccountId, categoryId,
                startDate, endDate, nextExecutionDate, lastExecutionDate, autoGenerate, active,
                description, notes, createdAt, LocalDateTime.now()
        );
    }

    public RecurringTransaction changeAmount(Money amount) {

        validateAmount(amount);

        return new RecurringTransaction(
                id, name, transactionType, frequency, amount, fromAccountId, toAccountId, categoryId,
                startDate, endDate, nextExecutionDate, lastExecutionDate, autoGenerate, active,
                description, notes, createdAt, LocalDateTime.now()
        );
    }

    public RecurringTransaction changeFrequency(Frequency frequency) {

        validateFrequency(frequency);

        return new RecurringTransaction(
                id, name, transactionType, frequency, amount, fromAccountId, toAccountId, categoryId,
                startDate, endDate, nextExecutionDate, lastExecutionDate, autoGenerate, active,
                description, notes, createdAt, LocalDateTime.now()
        );
    }

    public RecurringTransaction changeDateRange(LocalDate startDate,
                                                LocalDate endDate) {

        validateStartDate(startDate);
        validateDateRange(startDate, endDate);

        return new RecurringTransaction(
                id, name, transactionType, frequency, amount, fromAccountId, toAccountId, categoryId,
                startDate, endDate, nextExecutionDate, lastExecutionDate, autoGenerate, active,
                description, notes, createdAt, LocalDateTime.now()
        );
    }

    public RecurringTransaction changeDescription(String description) {

        return new RecurringTransaction(
                id, name, transactionType, frequency, amount, fromAccountId, toAccountId, categoryId,
                startDate, endDate, nextExecutionDate, lastExecutionDate, autoGenerate, active,
                description, notes, createdAt, LocalDateTime.now()
        );
    }

    public RecurringTransaction changeNotes(String notes) {

        return new RecurringTransaction(
                id, name, transactionType, frequency, amount, fromAccountId, toAccountId, categoryId,
                startDate, endDate, nextExecutionDate, lastExecutionDate, autoGenerate, active,
                description, notes, createdAt, LocalDateTime.now()
        );
    }

    public RecurringTransaction changeAutoGenerate(boolean autoGenerate) {

        return new RecurringTransaction(
                id, name, transactionType, frequency, amount, fromAccountId, toAccountId, categoryId,
                startDate, endDate, nextExecutionDate, lastExecutionDate, autoGenerate, active,
                description, notes, createdAt, LocalDateTime.now()
        );
    }

    /**
     * Records a successful generation of the currently-due occurrence and
     * advances the schedule from that occurrence's date (not "today"), so
     * the schedule stays anchored even if the run happens late.
     */
    public RecurringTransaction markExecuted() {

        return new RecurringTransaction(
                id, name, transactionType, frequency, amount, fromAccountId, toAccountId, categoryId,
                startDate, endDate, frequency.advance(nextExecutionDate), nextExecutionDate, autoGenerate, active,
                description, notes, createdAt, LocalDateTime.now()
        );
    }

    /**
     * Advances the schedule past a due occurrence that could not be
     * generated (e.g. no open salary cycle), without recording it as the
     * last successful execution.
     */
    public RecurringTransaction markSkipped() {

        return new RecurringTransaction(
                id, name, transactionType, frequency, amount, fromAccountId, toAccountId, categoryId,
                startDate, endDate, frequency.advance(nextExecutionDate), lastExecutionDate, autoGenerate, active,
                description, notes, createdAt, LocalDateTime.now()
        );
    }
}
