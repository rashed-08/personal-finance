package io.rashed.finance.domain.transactions;

import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class Transaction {

    private final TransactionId id;

    private final TransactionType transactionType;

    private final TransactionStatus transactionStatus;

    private final LocalDate transactionDate;

    private final Money amount;

    private final String description;

    private final String notes;

    private final AccountId fromAccountId;

    private final AccountId toAccountId;

    private final CategoryId categoryId;

    private final SalaryCycleId salaryCycleId;

    private final String referenceNumber;

    private final String migrationBatchId;

    private final String reconciliationBatchId;

    private final AdjustmentReason adjustmentReason;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public Transaction(TransactionId id, TransactionType transactionType, TransactionStatus transactionStatus, LocalDate transactionDate, Money amount, String description,
            String notes, AccountId fromAccountId, AccountId toAccountId, CategoryId categoryId, SalaryCycleId salaryCycleId, String referenceNumber, String migrationBatchId,
            String reconciliationBatchId, AdjustmentReason adjustmentReason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.transactionType = Objects.requireNonNull(transactionType);
        this.transactionStatus = Objects.requireNonNull(transactionStatus);
        this.transactionDate = Objects.requireNonNull(transactionDate);
        this.amount = Objects.requireNonNull(amount);

        this.description = description == null ? null : description.trim();;
        this.notes = notes == null ? null : notes.trim();;

        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.categoryId = categoryId;
        this.salaryCycleId = salaryCycleId;

        this.referenceNumber = referenceNumber;
        this.migrationBatchId = migrationBatchId;
        this.reconciliationBatchId = reconciliationBatchId;
        this.adjustmentReason = adjustmentReason;

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static Transaction expense(TransactionId id, LocalDate transactionDate, Money amount, 
        AccountId fromAccountId, CategoryId categoryId, SalaryCycleId salaryCycleId, String description) {

        validateAmount(amount);
        validateExpense(fromAccountId, categoryId);
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(id, TransactionType.EXPENSE, TransactionStatus.POSTED, transactionDate, amount, description, null,fromAccountId, 
            null, categoryId, salaryCycleId, null, null, null, null, now, now);
    }

    public static Transaction income(TransactionId id, LocalDate transactionDate, Money amount, 
        AccountId toAccountId, CategoryId categoryId, SalaryCycleId salaryCycleId, String description) {

        validateAmount(amount);
        validateIncome(toAccountId, categoryId);
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(id, TransactionType.INCOME, TransactionStatus.POSTED, transactionDate, amount, description, null, null, toAccountId,
            categoryId, salaryCycleId, null, null, null, null, now, now);

    }

    public static Transaction transfer(TransactionId id, LocalDate transactionDate, Money amount, 
        AccountId fromAccountId, AccountId toAccountId, SalaryCycleId salaryCycleId, String description) {

        validateAmount(amount);
        validateTransfer(fromAccountId, toAccountId);
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(id, TransactionType.TRANSFER, TransactionStatus.POSTED, transactionDate, amount, description, null, fromAccountId,
            toAccountId, null, salaryCycleId, null, null, null, null, now, now);
    }

    public static Transaction adjustment(TransactionId id, LocalDate transactionDate, Money amount, AccountId accountId, AdjustmentReason adjustmentReason, String description) {
        validateAmount(amount);
        validateAdjustment(accountId, adjustmentReason);
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(id, TransactionType.ADJUSTMENT, TransactionStatus.POSTED, transactionDate, amount, description, null, accountId,
            null, null, null, null, null, null, adjustmentReason, now, now);
    }

    public static Transaction openingBalance(TransactionId id, LocalDate transactionDate, Money amount, AccountId accountId) {
        validateAmount(amount);
        validateOpeningBalance(accountId);
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(id, TransactionType.OPENING_BALANCE, TransactionStatus.POSTED, transactionDate, amount, "Opening Balance", null,
            null, accountId, null, null, null, null, null, AdjustmentReason.OPENING_BALANCE, now, now);
    }

    public static Transaction migration(TransactionId id, LocalDate transactionDate, Money amount, AccountId accountId, String migrationBatchId, String description) {

        validateAmount(amount);
        validateMigration(accountId, migrationBatchId);
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(id, TransactionType.MIGRATION, TransactionStatus.POSTED, transactionDate, amount, description, null, null, accountId,
            null, null, null, migrationBatchId, null, AdjustmentReason.DATA_MIGRATION, now, now);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static void validateAmount(Money amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");

        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero.");
        }
    }

    private static void validateExpense(AccountId fromAccountId, CategoryId categoryId) {

        Objects.requireNonNull(fromAccountId, "Expense requires a source account.");

        Objects.requireNonNull(categoryId, "Expense requires a category.");
    }

    private static void validateIncome(AccountId toAccountId, CategoryId categoryId) {

        Objects.requireNonNull(toAccountId, "Income requires a destination account.");

        Objects.requireNonNull(categoryId, "Income requires a category.");
    }

    private static void validateTransfer(AccountId fromAccountId, AccountId toAccountId) {

        Objects.requireNonNull(fromAccountId, "Transfer requires a source account.");

        Objects.requireNonNull(toAccountId, "Transfer requires a destination account.");

        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same.");
        }
    }

    private static void validateAdjustment(AccountId accountId, AdjustmentReason adjustmentReason) {

        Objects.requireNonNull(accountId, "Adjustment requires an account.");

        Objects.requireNonNull(adjustmentReason, "Adjustment reason is required.");
    }

    private static void validateOpeningBalance(AccountId accountId) {

        Objects.requireNonNull(accountId, "Opening balance requires an account.");
    }

    private static void validateMigration(AccountId accountId,String migrationBatchId) {

        Objects.requireNonNull(accountId, "Migration requires an account.");

        if (migrationBatchId == null || migrationBatchId.isBlank()) {
            throw new IllegalArgumentException("Migration batch id is required.");
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

    public boolean isAdjustment() {
        return transactionType == TransactionType.ADJUSTMENT;
    }

    public boolean isOpeningBalance() {
        return transactionType == TransactionType.OPENING_BALANCE;
    }

    public boolean isMigration() {
        return transactionType == TransactionType.MIGRATION;
    }

    public boolean isPosted() {
        return transactionStatus == TransactionStatus.POSTED;
    }

    public boolean isVoided() {
        return transactionStatus == TransactionStatus.VOID;
    }

    public boolean isReversed() {
        return transactionStatus == TransactionStatus.REVERSED;
    }

    public boolean hasCategory() {
        return categoryId != null;
    }

    public boolean hasSalaryCycle() {
        return salaryCycleId != null;
    }

    public boolean hasReferenceNumber() {
        return referenceNumber != null && !referenceNumber.isBlank();
    }

    public boolean hasFromAccount() {
        return fromAccountId != null;
    }

    public boolean hasToAccount() {
        return toAccountId != null;
    }

    public Transaction post() {

        if (isPosted()) {
            return this;
        }

        return new Transaction(id, transactionType, TransactionStatus.POSTED, transactionDate, amount, description, notes, fromAccountId, toAccountId,
                categoryId, salaryCycleId, referenceNumber, migrationBatchId, reconciliationBatchId, adjustmentReason, createdAt, LocalDateTime.now());
    }

    public Transaction voidTransaction() {

        if (isVoided()) {
            return this;
        }

        if (isReversed()) {
            throw new IllegalStateException("A reversed transaction cannot be voided.");
        }

        return new Transaction(id, transactionType, TransactionStatus.VOID, transactionDate, amount, description, notes, fromAccountId, toAccountId,
                categoryId, salaryCycleId, referenceNumber, migrationBatchId, reconciliationBatchId, adjustmentReason, createdAt, LocalDateTime.now());
    }

    public Transaction reverse() {

        if (isReversed()) {
            return this;
        }

        if (isVoided()) {
            throw new IllegalStateException("A voided transaction cannot be reversed.");
        }

        return new Transaction(id, transactionType, TransactionStatus.REVERSED, transactionDate, amount, description, notes, fromAccountId,
                toAccountId, categoryId, salaryCycleId, referenceNumber, migrationBatchId, reconciliationBatchId, adjustmentReason, createdAt, LocalDateTime.now());
    }
}