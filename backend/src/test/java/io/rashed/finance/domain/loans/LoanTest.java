package io.rashed.finance.domain.loans;

import io.rashed.finance.common.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanTest {

    private final LocalDate today = LocalDate.of(2026, 7, 25);
    private final LocalDate futureDueDate = today.plusMonths(6);

    @Test
    void receivable_startsActive() {

        Loan loan = Loan.receivable("Rahim", Money.of(10000), today, futureDueDate, null);

        assertTrue(loan.isReceivable());
        assertFalse(loan.isPayable());
        assertTrue(loan.isActive());
        assertEquals(Money.of(10000), loan.getPrincipalAmount());
        assertEquals(today, loan.getStartDate());
        assertEquals(futureDueDate, loan.getDueDate());
    }

    @Test
    void payable_startsActive() {

        Loan loan = Loan.payable("Brother", Money.of(25000), today, null, null);

        assertTrue(loan.isPayable());
        assertFalse(loan.isReceivable());
        assertTrue(loan.isActive());
        assertFalse(loan.hasDueDate());
    }

    @Test
    void create_rejectsBlankName() {

        assertThrows(IllegalArgumentException.class, () ->
                Loan.receivable("  ", Money.of(1000), today, null, null));
    }

    @Test
    void create_rejectsZeroPrincipal() {

        assertThrows(IllegalArgumentException.class, () ->
                Loan.receivable("Rahim", Money.zero(), today, null, null));
    }

    @Test
    void create_rejectsNegativePrincipal() {

        assertThrows(IllegalArgumentException.class, () ->
                Loan.receivable("Rahim", Money.of(-100), today, null, null));
    }

    @Test
    void create_rejectsPastDueDate() {

        assertThrows(IllegalArgumentException.class, () ->
                Loan.receivable("Rahim", Money.of(1000), today, today.minusDays(1), null));
    }

    @Test
    void create_requiresStartDate() {

        assertThrows(NullPointerException.class, () ->
                Loan.receivable("Rahim", Money.of(1000), null, null, null));
    }

    @Test
    void rename_changesNamePreservingIdentity() {

        Loan loan = Loan.receivable("Old Name", Money.of(1000), today, null, null);

        Loan renamed = loan.rename("New Name");

        assertEquals("New Name", renamed.getName());
        assertEquals(loan.getId(), renamed.getId());
    }

    @Test
    void changeDueDate_replacesDueDate() {

        Loan loan = Loan.receivable("Rahim", Money.of(1000), today, null, null);

        Loan changed = loan.changeDueDate(futureDueDate);

        assertEquals(futureDueDate, changed.getDueDate());
    }

    @Test
    void changeDueDate_rejectsPastDate() {

        Loan loan = Loan.receivable("Rahim", Money.of(1000), today, null, null);

        assertThrows(IllegalArgumentException.class, () -> loan.changeDueDate(today.minusDays(1)));
    }

    @Test
    void close_transitionsFromActiveToClosed() {

        Loan loan = Loan.receivable("Rahim", Money.of(1000), today, null, null);

        Loan closed = loan.close();

        assertTrue(closed.isClosed());
        assertFalse(closed.isActive());
    }

    @Test
    void close_isIdempotent() {

        Loan closed = Loan.receivable("Rahim", Money.of(1000), today, null, null).close();

        assertSame(closed, closed.close());
    }

    @Test
    void close_rejectsCancelledLoan() {

        Loan cancelled = Loan.receivable("Rahim", Money.of(1000), today, null, null).cancel();

        assertThrows(IllegalStateException.class, cancelled::close);
    }

    @Test
    void cancel_transitionsFromActiveToCancelled() {

        Loan loan = Loan.payable("Friend", Money.of(3000), today, null, null);

        Loan cancelled = loan.cancel();

        assertTrue(cancelled.isCancelled());
    }

    @Test
    void cancel_isIdempotent() {

        Loan cancelled = Loan.payable("Friend", Money.of(3000), today, null, null).cancel();

        assertSame(cancelled, cancelled.cancel());
    }

    @Test
    void cancel_rejectsClosedLoan() {

        Loan closed = Loan.payable("Friend", Money.of(3000), today, null, null).close();

        assertThrows(IllegalStateException.class, closed::cancel);
    }

    @Test
    void changeDescription_updatesDescription() {

        Loan loan = Loan.receivable("Rahim", Money.of(1000), today, null, null);

        Loan updated = loan.changeDescription("Emergency loan");

        assertEquals("Emergency loan", updated.getDescription());
        assertTrue(updated.hasDescription());
    }
}
