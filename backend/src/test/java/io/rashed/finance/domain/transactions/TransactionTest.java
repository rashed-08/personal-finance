package io.rashed.finance.domain.transactions;

import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    private final LocalDate today = LocalDate.of(2026, 7, 25);
    private final AccountId account = AccountId.newId();
    private final AccountId otherAccount = AccountId.newId();
    private final CategoryId category = CategoryId.newId();
    private final SalaryCycleId salaryCycle = SalaryCycleId.newId();

    // -------------------------------------------------------------------------
    // Expense
    // -------------------------------------------------------------------------

    @Test
    void expense_createsPostedTransactionWithFromAccount() {

        Transaction expense = Transaction.expense(
                TransactionId.newId(), today, Money.of(500), account, category, salaryCycle, "Groceries");

        assertEquals(TransactionType.EXPENSE, expense.getTransactionType());
        assertEquals(TransactionStatus.POSTED, expense.getTransactionStatus());
        assertEquals(account, expense.getFromAccountId());
        assertNull(expense.getToAccountId());
        assertEquals(category, expense.getCategoryId());
        assertTrue(expense.isExpense());
    }

    @Test
    void expense_rejectsMissingAccount() {

        assertThrows(NullPointerException.class, () ->
                Transaction.expense(TransactionId.newId(), today, Money.of(500), null, category, salaryCycle, "x"));
    }

    @Test
    void expense_rejectsMissingCategory() {

        assertThrows(NullPointerException.class, () ->
                Transaction.expense(TransactionId.newId(), today, Money.of(500), account, null, salaryCycle, "x"));
    }

    @Test
    void expense_rejectsZeroAmount() {

        assertThrows(IllegalArgumentException.class, () ->
                Transaction.expense(TransactionId.newId(), today, Money.zero(), account, category, salaryCycle, "x"));
    }

    @Test
    void expense_rejectsNegativeAmount() {

        assertThrows(IllegalArgumentException.class, () ->
                Transaction.expense(TransactionId.newId(), today, Money.of(-100), account, category, salaryCycle, "x"));
    }

    // -------------------------------------------------------------------------
    // Income
    // -------------------------------------------------------------------------

    @Test
    void income_createsPostedTransactionWithToAccount() {

        Transaction income = Transaction.income(
                TransactionId.newId(), today, Money.of(80000), account, category, salaryCycle, "Salary");

        assertTrue(income.isIncome());
        assertEquals(account, income.getToAccountId());
        assertNull(income.getFromAccountId());
    }

    @Test
    void income_rejectsMissingDestinationAccount() {

        assertThrows(NullPointerException.class, () ->
                Transaction.income(TransactionId.newId(), today, Money.of(500), null, category, salaryCycle, "x"));
    }

    // -------------------------------------------------------------------------
    // Transfer
    // -------------------------------------------------------------------------

    @Test
    void transfer_movesBetweenTwoAccounts() {

        Transaction transfer = Transaction.transfer(
                TransactionId.newId(), today, Money.of(1000), account, otherAccount, salaryCycle, "ATM");

        assertTrue(transfer.isTransfer());
        assertEquals(account, transfer.getFromAccountId());
        assertEquals(otherAccount, transfer.getToAccountId());
        assertNull(transfer.getCategoryId());
    }

    @Test
    void transfer_rejectsSameSourceAndDestination() {

        assertThrows(IllegalArgumentException.class, () ->
                Transaction.transfer(TransactionId.newId(), today, Money.of(1000), account, account, salaryCycle, "x"));
    }

    // -------------------------------------------------------------------------
    // Adjustment
    // -------------------------------------------------------------------------

    @Test
    void adjustment_allowsNoAccountAtAll() {

        Transaction adjustment = Transaction.adjustment(
                TransactionId.newId(), today, Money.of(300), null, null, null,
                AdjustmentReason.SYSTEM_CORRECTION, "note only", null);

        assertTrue(adjustment.isAdjustment());
        assertNull(adjustment.getFromAccountId());
        assertNull(adjustment.getToAccountId());
    }

    @Test
    void adjustment_rejectsBothAccountsSet() {

        assertThrows(IllegalArgumentException.class, () ->
                Transaction.adjustment(TransactionId.newId(), today, Money.of(300), account, otherAccount, null,
                        AdjustmentReason.CASH_RECONCILIATION, "x", null));
    }

    @Test
    void adjustment_rejectsMissingReason() {

        assertThrows(NullPointerException.class, () ->
                Transaction.adjustment(TransactionId.newId(), today, Money.of(300), account, null, null,
                        null, "x", null));
    }

    @Test
    void adjustment_manualCorrectionRequiresNotes() {

        assertThrows(IllegalArgumentException.class, () ->
                Transaction.adjustment(TransactionId.newId(), today, Money.of(300), account, null, null,
                        AdjustmentReason.MANUAL_CORRECTION, "desc", null));

        assertThrows(IllegalArgumentException.class, () ->
                Transaction.adjustment(TransactionId.newId(), today, Money.of(300), account, null, null,
                        AdjustmentReason.MANUAL_CORRECTION, "desc", "   "));
    }

    @Test
    void adjustment_manualCorrectionSucceedsWithNotes() {

        Transaction adjustment = Transaction.adjustment(
                TransactionId.newId(), today, Money.of(300), account, null, null,
                AdjustmentReason.MANUAL_CORRECTION, "desc", "explained here");

        assertEquals("explained here", adjustment.getNotes());
    }

    @Test
    void adjustment_otherReasonsDoNotRequireNotes() {

        Transaction adjustment = Transaction.adjustment(
                TransactionId.newId(), today, Money.of(300), account, null, null,
                AdjustmentReason.CASH_RECONCILIATION, "desc", null);

        assertNull(adjustment.getNotes());
    }

    // -------------------------------------------------------------------------
    // Opening Balance / Migration
    // -------------------------------------------------------------------------

    @Test
    void openingBalance_requiresAccount() {

        assertThrows(NullPointerException.class, () ->
                Transaction.openingBalance(TransactionId.newId(), today, Money.of(1000), null));
    }

    @Test
    void migration_requiresBatchId() {

        assertThrows(IllegalArgumentException.class, () ->
                Transaction.migration(TransactionId.newId(), today, Money.of(1000), account, "", "x"));

        assertThrows(IllegalArgumentException.class, () ->
                Transaction.migration(TransactionId.newId(), today, Money.of(1000), account, null, "x"));
    }

    // -------------------------------------------------------------------------
    // Lifecycle: post / void / reverse
    // -------------------------------------------------------------------------

    @Test
    void voidTransaction_transitionsFromPostedToVoid() {

        Transaction expense = anExpense();

        Transaction voided = expense.voidTransaction();

        assertTrue(voided.isVoided());
    }

    @Test
    void voidTransaction_isIdempotent() {

        Transaction voided = anExpense().voidTransaction();

        assertSame(voided, voided.voidTransaction());
    }

    @Test
    void voidTransaction_rejectsReversedTransaction() {

        Transaction reversed = anExpense().reverse();

        assertThrows(IllegalStateException.class, reversed::voidTransaction);
    }

    @Test
    void reverse_transitionsFromPostedToReversed() {

        Transaction reversed = anExpense().reverse();

        assertTrue(reversed.isReversed());
    }

    @Test
    void reverse_isIdempotent() {

        Transaction reversed = anExpense().reverse();

        assertSame(reversed, reversed.reverse());
    }

    @Test
    void reverse_rejectsVoidedTransaction() {

        Transaction voided = anExpense().voidTransaction();

        assertThrows(IllegalStateException.class, voided::reverse);
    }

    // -------------------------------------------------------------------------
    // withDetails
    // -------------------------------------------------------------------------

    @Test
    void withDetails_updatesDescriptiveFieldsInPlace() {

        Transaction expense = anExpense();
        CategoryId newCategory = CategoryId.newId();

        Transaction updated = expense.withDetails(newCategory, "new description", "new notes");

        assertEquals(newCategory, updated.getCategoryId());
        assertEquals("new description", updated.getDescription());
        assertEquals("new notes", updated.getNotes());
        assertEquals(expense.getAmount(), updated.getAmount());
        assertEquals(expense.getId(), updated.getId());
    }

    @Test
    void withDetails_rejectsClearingCategoryOnExpense() {

        assertThrows(IllegalArgumentException.class, () ->
                anExpense().withDetails(null, "d", "n"));
    }

    @Test
    void withDetails_rejectsClearingCategoryOnIncome() {

        Transaction income = Transaction.income(
                TransactionId.newId(), today, Money.of(500), account, category, salaryCycle, "x");

        assertThrows(IllegalArgumentException.class, () ->
                income.withDetails(null, "d", "n"));
    }

    // -------------------------------------------------------------------------
    // increasesBalance / affectedAccountId
    // -------------------------------------------------------------------------

    @Test
    void increasesBalance_isTrueForIncomeOpeningBalanceAndMigration() {

        assertTrue(Transaction.income(TransactionId.newId(), today, Money.of(1), account, category, salaryCycle, null)
                .increasesBalance());

        assertTrue(Transaction.openingBalance(TransactionId.newId(), today, Money.of(1), account)
                .increasesBalance());

        assertTrue(Transaction.migration(TransactionId.newId(), today, Money.of(1), account, "batch-1", null)
                .increasesBalance());
    }

    @Test
    void increasesBalance_isFalseForExpense() {

        assertFalse(anExpense().increasesBalance());
    }

    @Test
    void increasesBalance_forAdjustmentDependsOnWhichSideIsSet() {

        Transaction increase = Transaction.adjustment(
                TransactionId.newId(), today, Money.of(1), null, account, null,
                AdjustmentReason.CASH_RECONCILIATION, "d", null);

        Transaction decrease = Transaction.adjustment(
                TransactionId.newId(), today, Money.of(1), account, null, null,
                AdjustmentReason.CASH_RECONCILIATION, "d", null);

        assertTrue(increase.increasesBalance());
        assertFalse(decrease.increasesBalance());
    }

    @Test
    void increasesBalance_rejectsTransfer() {

        Transaction transfer = Transaction.transfer(
                TransactionId.newId(), today, Money.of(1), account, otherAccount, salaryCycle, null);

        assertThrows(IllegalStateException.class, transfer::increasesBalance);
    }

    @Test
    void affectedAccountId_returnsWhicheverSideIsPopulated() {

        assertEquals(account, anExpense().affectedAccountId());

        Transaction income = Transaction.income(
                TransactionId.newId(), today, Money.of(1), account, category, salaryCycle, null);

        assertEquals(account, income.affectedAccountId());
    }

    @Test
    void affectedAccountId_rejectsTransfer() {

        Transaction transfer = Transaction.transfer(
                TransactionId.newId(), today, Money.of(1), account, otherAccount, salaryCycle, null);

        assertThrows(IllegalStateException.class, transfer::affectedAccountId);
    }

    // -------------------------------------------------------------------------
    // Reference transaction linkage
    // -------------------------------------------------------------------------

    @Test
    void isAdjustmentFor_matchesReferenceTransactionId() {

        TransactionId originalId = TransactionId.newId();

        Transaction adjustment = Transaction.adjustment(
                TransactionId.newId(), today, Money.of(50), account, null, originalId,
                AdjustmentReason.TRANSACTION_UPDATE, "d", null);

        assertTrue(adjustment.isAdjustmentFor(originalId));
        assertFalse(adjustment.isAdjustmentFor(TransactionId.newId()));
        assertTrue(adjustment.hasReferenceTransaction());
    }

    private Transaction anExpense() {

        return Transaction.expense(
                TransactionId.newId(), today, Money.of(500), account, category, salaryCycle, "Groceries");
    }
}
