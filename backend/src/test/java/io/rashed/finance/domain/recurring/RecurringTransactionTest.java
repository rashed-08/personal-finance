package io.rashed.finance.domain.recurring;

import io.rashed.finance.common.enums.Frequency;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecurringTransactionTest {

    private final AccountId account = AccountId.newId();
    private final AccountId otherAccount = AccountId.newId();
    private final CategoryId category = CategoryId.newId();
    private final LocalDate startDate = LocalDate.of(2026, 1, 1);

    @Test
    void expense_startsActiveWithNextExecutionEqualToStartDate() {

        RecurringTransaction template = RecurringTransaction.expense(
                "House Rent", account, category, Money.of(10000), Frequency.MONTHLY, startDate, null, false, null, null);

        assertTrue(template.isExpense());
        assertTrue(template.isActive());
        assertEquals(startDate, template.getNextExecutionDate());
        assertNull(template.getLastExecutionDate());
        assertFalse(template.isAutoGenerate());
    }

    @Test
    void income_requiresDestinationAccountAndCategory() {

        assertThrows(NullPointerException.class, () ->
                RecurringTransaction.income("Salary", null, category, Money.of(1000), Frequency.MONTHLY, startDate, null, true, null, null));
    }

    @Test
    void transfer_requiresBothAccounts() {

        RecurringTransaction template = RecurringTransaction.transfer(
                "Savings Sweep", account, otherAccount, Money.of(500), Frequency.MONTHLY, startDate, null, true, null, null);

        assertTrue(template.isTransfer());
        assertNull(template.getCategoryId());

        assertThrows(NullPointerException.class, () ->
                RecurringTransaction.transfer("x", account, null, Money.of(500), Frequency.MONTHLY, startDate, null, true, null, null));
    }

    @Test
    void transfer_rejectsSameAccountBothSides() {

        assertThrows(IllegalArgumentException.class, () ->
                RecurringTransaction.transfer("x", account, account, Money.of(500), Frequency.MONTHLY, startDate, null, true, null, null));
    }

    @Test
    void create_rejectsBlankName() {

        assertThrows(IllegalArgumentException.class, () ->
                RecurringTransaction.expense("  ", account, category, Money.of(1000), Frequency.MONTHLY, startDate, null, false, null, null));
    }

    @Test
    void create_rejectsEndDateBeforeStartDate() {

        assertThrows(IllegalArgumentException.class, () ->
                RecurringTransaction.expense("x", account, category, Money.of(1000), Frequency.MONTHLY, startDate, startDate.minusDays(1), false, null, null));
    }

    @Test
    void markExecuted_advancesFromNextExecutionDateAndSetsLastExecutionDate() {

        RecurringTransaction template = anExpense();

        RecurringTransaction executed = template.markExecuted();

        assertEquals(startDate, executed.getLastExecutionDate());
        assertEquals(Frequency.MONTHLY.advance(startDate), executed.getNextExecutionDate());
    }

    @Test
    void markExecuted_anchorsOnScheduledDateNotToday() {

        // Simulate two consecutive occurrences via markExecuted, confirming
        // the schedule stays anchored on the 1st regardless of call timing.
        RecurringTransaction template = anExpense();

        RecurringTransaction afterFirst = template.markExecuted();
        RecurringTransaction afterSecond = afterFirst.markExecuted();

        assertEquals(LocalDate.of(2026, 2, 1), afterFirst.getNextExecutionDate());
        assertEquals(LocalDate.of(2026, 3, 1), afterSecond.getNextExecutionDate());
    }

    @Test
    void markSkipped_advancesNextExecutionDateButNotLastExecutionDate() {

        RecurringTransaction template = anExpense();

        RecurringTransaction skipped = template.markSkipped();

        assertEquals(Frequency.MONTHLY.advance(startDate), skipped.getNextExecutionDate());
        assertNull(skipped.getLastExecutionDate());
    }

    @Test
    void isDue_trueWhenNextExecutionDateNotAfterAsOfDate() {

        RecurringTransaction template = anExpense();

        assertTrue(template.isDue(startDate));
        assertTrue(template.isDue(startDate.plusDays(1)));
        assertFalse(template.isDue(startDate.minusDays(1)));
    }

    @Test
    void isDue_falseWhenInactive() {

        RecurringTransaction template = anExpense().deactivate();

        assertFalse(template.isDue(startDate));
    }

    @Test
    void isDue_falseWhenPastEndDate() {

        RecurringTransaction template = RecurringTransaction.expense(
                "x", account, category, Money.of(1000), Frequency.MONTHLY, startDate, startDate, false, null, null);

        RecurringTransaction afterEnd = template.markExecuted();

        assertFalse(afterEnd.isDue(afterEnd.getNextExecutionDate()));
    }

    @Test
    void activate_isIdempotent() {

        RecurringTransaction template = anExpense();

        assertSame(template, template.activate());
    }

    @Test
    void deactivate_isIdempotent() {

        RecurringTransaction deactivated = anExpense().deactivate();

        assertSame(deactivated, deactivated.deactivate());
    }

    @Test
    void changeAmount_updatesAmount() {

        RecurringTransaction updated = anExpense().changeAmount(Money.of(20000));

        assertEquals(Money.of(20000), updated.getAmount());
    }

    @Test
    void changeAmount_rejectsZero() {

        assertThrows(IllegalArgumentException.class, () -> anExpense().changeAmount(Money.zero()));
    }

    private RecurringTransaction anExpense() {

        return RecurringTransaction.expense(
                "House Rent", account, category, Money.of(10000), Frequency.MONTHLY, startDate, null, false, null, null);
    }
}
