package io.rashed.finance.domain.transactions;

import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.loans.LoanId;
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

    private final TransactionId referenceTransactionId;

    /**
     * Set only for a TRANSFER representing a fund allocation or withdrawal:
     * exactly one of fromAccountId/toAccountId is set (the real account
     * side), and this identifies the fund on the other side. Null for
     * every other transaction.
     */
    private final FundId fundId;

    /**
     * Set only for a TRANSFER representing a loan disbursement/receipt or
     * repayment: exactly one of fromAccountId/toAccountId is set (the real
     * account side), and this identifies the loan on the other side. Never
     * set together with fundId. Null for every other transaction.
     */
    private final LoanId loanId;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public Transaction(TransactionId id, TransactionType transactionType, TransactionStatus transactionStatus, LocalDate transactionDate, Money amount, String description,
            String notes, AccountId fromAccountId, AccountId toAccountId, CategoryId categoryId, SalaryCycleId salaryCycleId, String referenceNumber, String migrationBatchId,
            String reconciliationBatchId, AdjustmentReason adjustmentReason, TransactionId referenceTransactionId, FundId fundId, LoanId loanId, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
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
        this.referenceTransactionId = referenceTransactionId;
        this.adjustmentReason = adjustmentReason;
        this.fundId = fundId;
        this.loanId = loanId;

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
            null, categoryId, salaryCycleId, null, null, null, null, null, null, null, now, now);
    }

    /**
     * An expense backfilled from a legacy source (e.g. Google Keep, CSV).
     * Behaves exactly like {@link #expense}, tagged with a migration batch
     * id for later auditing/rollback, per docs/database/tables/transactions/02-transaction-types.md.
     */
    public static Transaction importedExpense(TransactionId id, LocalDate transactionDate, Money amount,
        AccountId fromAccountId, CategoryId categoryId, SalaryCycleId salaryCycleId, String description, String notes, String migrationBatchId) {

        validateAmount(amount);
        validateExpense(fromAccountId, categoryId);
        Objects.requireNonNull(migrationBatchId, "Imported expense requires a migration batch id.");
        if (migrationBatchId.isBlank()) {
            throw new IllegalArgumentException("Migration batch id cannot be blank.");
        }
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(id, TransactionType.EXPENSE, TransactionStatus.POSTED, transactionDate, amount, description, notes, fromAccountId,
            null, categoryId, salaryCycleId, null, migrationBatchId, null, null, null, null, null, now, now);
    }

    public static Transaction income(TransactionId id, LocalDate transactionDate, Money amount,
        AccountId toAccountId, CategoryId categoryId, SalaryCycleId salaryCycleId, String description) {

        validateAmount(amount);
        validateIncome(toAccountId, categoryId);
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(id, TransactionType.INCOME, TransactionStatus.POSTED, transactionDate, amount, description, null, null, toAccountId,
            categoryId, salaryCycleId, null, null, null, null, null, null, null, now, now);

    }

    /** Ordinary account-to-account transfer. Use {@link #fundTransfer} / {@link #loanTransfer} for those variants. */
    public static Transaction transfer(TransactionId id, LocalDate transactionDate, Money amount,
        AccountId fromAccountId, AccountId toAccountId, SalaryCycleId salaryCycleId, String description) {

        validateAmount(amount);
        validateTransfer(fromAccountId, toAccountId, null, null);
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(id, TransactionType.TRANSFER, TransactionStatus.POSTED, transactionDate, amount, description, null, fromAccountId,
            toAccountId, null, salaryCycleId, null, null, null, null, null, null, null, now, now);
    }

    /**
     * A fund allocation or withdrawal. Exactly one of fromAccountId/toAccountId
     * must be set — the real account on one side, the fund implicitly on the
     * other:
     * <ul>
     *   <li>Allocation (account -&gt; fund): pass fromAccountId, leave toAccountId null.</li>
     *   <li>Withdrawal (fund -&gt; account): pass toAccountId, leave fromAccountId null.</li>
     * </ul>
     */
    public static Transaction fundTransfer(TransactionId id, LocalDate transactionDate, Money amount,
        AccountId fromAccountId, AccountId toAccountId, FundId fundId, SalaryCycleId salaryCycleId, String description) {

        validateAmount(amount);
        Objects.requireNonNull(fundId, "Fund transfer requires a fund.");
        validateTransfer(fromAccountId, toAccountId, fundId, null);
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(id, TransactionType.TRANSFER, TransactionStatus.POSTED, transactionDate, amount, description, null, fromAccountId,
            toAccountId, null, salaryCycleId, null, null, null, null, null, fundId, null, now, now);
    }

    /**
     * A loan disbursement/receipt or repayment. Exactly one of
     * fromAccountId/toAccountId must be set — the real account on one side,
     * the loan implicitly on the other:
     * <ul>
     *   <li>Money leaves the account (giving a receivable loan, or repaying a payable loan):
     *       pass fromAccountId, leave toAccountId null.</li>
     *   <li>Money enters the account (receiving a payable loan, or collecting a receivable loan):
     *       pass toAccountId, leave fromAccountId null.</li>
     * </ul>
     */
    public static Transaction loanTransfer(TransactionId id, LocalDate transactionDate, Money amount,
        AccountId fromAccountId, AccountId toAccountId, LoanId loanId, SalaryCycleId salaryCycleId, String description) {

        validateAmount(amount);
        Objects.requireNonNull(loanId, "Loan transfer requires a loan.");
        validateTransfer(fromAccountId, toAccountId, null, loanId);
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(id, TransactionType.TRANSFER, TransactionStatus.POSTED, transactionDate, amount, description, null, fromAccountId,
            toAccountId, null, salaryCycleId, null, null, null, null, null, null, loanId, now, now);
    }

    public static Transaction adjustment(TransactionId id, LocalDate transactionDate, Money amount, AccountId fromAccountId, AccountId toAccountId, TransactionId referenceTransactionId, AdjustmentReason adjustmentReason, String description, String notes) {
        validateAmount(amount);
        validateAdjustment(fromAccountId, toAccountId, adjustmentReason, notes);
        LocalDateTime now = LocalDateTime.now();

        return new Transaction(
                id,
                TransactionType.ADJUSTMENT,
                TransactionStatus.POSTED,
                transactionDate,
                amount,
                description,
                notes,
                fromAccountId,
                toAccountId,
                null,
                null,
                null,
                null,
                null,
                adjustmentReason,
                referenceTransactionId,
                null,
                null,
                now,
                now
        );
    }

    public static Transaction openingBalance(TransactionId id, LocalDate transactionDate, Money amount, AccountId accountId) {
        validateAmount(amount);
        validateOpeningBalance(accountId);
        LocalDateTime now = LocalDateTime.now();

                return new Transaction(
                id,
                TransactionType.OPENING_BALANCE,
                TransactionStatus.POSTED,
                transactionDate,
                amount,
                "Opening Balance",
                null,
                null,
                accountId,
                null,
                null,
                null,
                null,
                null,
                AdjustmentReason.OPENING_BALANCE,
                null,
                null,
                null,
                now,
                now
        );
    }

    public static Transaction migration(TransactionId id, LocalDate transactionDate, Money amount, AccountId accountId, String migrationBatchId, String description) {

        validateAmount(amount);
        validateMigration(accountId, migrationBatchId);
        LocalDateTime now = LocalDateTime.now();

        return new Transaction(
                id,
                TransactionType.MIGRATION,
                TransactionStatus.POSTED,
                transactionDate,
                amount,
                description,
                null,
                null,
                accountId,
                null,
                null,
                null,
                migrationBatchId,
                null,
                AdjustmentReason.DATA_MIGRATION,
                null,
                null,
                null,
                now,
                now
        );
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

    private static void validateTransfer(AccountId fromAccountId, AccountId toAccountId, FundId fundId, LoanId loanId) {

        if (fundId != null || loanId != null) {

            if ((fromAccountId == null) == (toAccountId == null)) {
                throw new IllegalArgumentException(
                        "A fund/loan transfer requires exactly one of fromAccountId or toAccountId.");
            }

            return;
        }

        Objects.requireNonNull(fromAccountId, "Transfer requires a source account.");

        Objects.requireNonNull(toAccountId, "Transfer requires a destination account.");

        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same.");
        }
    }

    private static void validateAdjustment(AccountId fromAccountId, AccountId toAccountId, AdjustmentReason adjustmentReason, String notes) {

        Objects.requireNonNull(adjustmentReason, "Adjustment reason is required.");

        if (fromAccountId != null && toAccountId != null) {
            throw new IllegalArgumentException(
                    "Adjustment cannot reference both fromAccountId and toAccountId.");
        }

        if (adjustmentReason == AdjustmentReason.MANUAL_CORRECTION && (notes == null || notes.isBlank())) {
            throw new IllegalArgumentException("Manual correction requires notes explaining the adjustment.");
        }
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

    public boolean hasFund() {
        return fundId != null;
    }

    public boolean hasLoan() {
        return loanId != null;
    }

    public Transaction withDetails(CategoryId categoryId, String description, String notes) {

        if ((isExpense() || isIncome()) && categoryId == null) {
            throw new IllegalArgumentException(
                    (isExpense() ? "Expense" : "Income") + " requires a category.");
        }

        return new Transaction(id, transactionType, transactionStatus, transactionDate, amount, description, notes, fromAccountId, toAccountId,
                categoryId, salaryCycleId, referenceNumber, migrationBatchId, reconciliationBatchId, adjustmentReason, referenceTransactionId, fundId, loanId, createdAt, LocalDateTime.now());
    }

    public Transaction post() {

        if (isPosted()) {
            return this;
        }

        return new Transaction(id, transactionType, TransactionStatus.POSTED, transactionDate, amount, description, notes, fromAccountId, toAccountId,
                categoryId, salaryCycleId, referenceNumber, migrationBatchId, reconciliationBatchId, adjustmentReason, referenceTransactionId, fundId, loanId, createdAt, LocalDateTime.now());
    }

    public Transaction voidTransaction() {

        if (isVoided()) {
            return this;
        }

        if (isReversed()) {
            throw new IllegalStateException("A reversed transaction cannot be voided.");
        }

        return new Transaction(
                id,
                transactionType,
                TransactionStatus.VOID,
                transactionDate,
                amount,
                description,
                notes,
                fromAccountId,
                toAccountId,
                categoryId,
                salaryCycleId,
                referenceNumber,
                migrationBatchId,
                reconciliationBatchId,
                adjustmentReason,
                referenceTransactionId,
                fundId,
                loanId,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Transaction reverse() {

        if (isReversed()) {
            return this;
        }

        if (isVoided()) {
            throw new IllegalStateException("A voided transaction cannot be reversed.");
        }

        return new Transaction(id, transactionType, TransactionStatus.REVERSED, transactionDate, amount, description, notes, fromAccountId,
                toAccountId, categoryId, salaryCycleId, referenceNumber, migrationBatchId, reconciliationBatchId, adjustmentReason, referenceTransactionId, fundId, loanId, createdAt, LocalDateTime.now());
    }

    public boolean isAdjustmentFor(TransactionId id) {

        return referenceTransactionId != null && referenceTransactionId.equals(id);
    }

    public boolean hasReferenceTransaction(){

        return referenceTransactionId != null;
    }

    /**
     * Whether this transaction increases (true) or decreases (false) the
     * balance of {@link #affectedAccountId()}. Transfers touch two accounts
     * in opposite directions and therefore have no single answer.
     */
    public boolean increasesBalance() {

        return switch (transactionType) {

            case INCOME, OPENING_BALANCE, MIGRATION -> true;

            case EXPENSE -> false;

            case ADJUSTMENT -> hasToAccount();

            case TRANSFER -> throw new IllegalStateException(
                    "Transfer affects two accounts and has no single balance direction.");
        };
    }

    /**
     * The single account this transaction moves money into or out of.
     * Transfers touch two accounts and therefore have no single answer.
     */
    public AccountId affectedAccountId() {

        if (isTransfer()) {
            throw new IllegalStateException(
                    "Transfer affects two accounts and has no single affected account.");
        }

        if (hasFromAccount()) {
            return fromAccountId;
        }

        if (hasToAccount()) {
            return toAccountId;
        }

        throw new IllegalStateException("Transaction has no associated account.");
    }

    /**
     * This transaction's signed contribution to the given account's balance:
     * positive if the account received the money, negative if it left the
     * account. Unlike {@link #increasesBalance()}, this works uniformly
     * across every transaction type including transfers, since it asks
     * about one specific account rather than the transaction as a whole.
     */
    public Money signedAmountFor(AccountId accountId) {

        Objects.requireNonNull(accountId, "Account cannot be null.");

        if (accountId.equals(toAccountId)) {
            return amount;
        }

        if (accountId.equals(fromAccountId)) {
            return amount.negate();
        }

        throw new IllegalArgumentException("Transaction does not involve this account.");
    }

    /**
     * Whether this fund transfer increases (allocation) or decreases
     * (withdrawal) the linked fund's reserved balance.
     */
    public boolean increasesFundBalance() {

        if (!hasFund()) {
            throw new IllegalStateException("Transaction is not linked to a fund.");
        }

        return toAccountId == null;
    }
}
