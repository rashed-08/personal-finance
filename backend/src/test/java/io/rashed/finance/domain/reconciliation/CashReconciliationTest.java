package io.rashed.finance.domain.reconciliation;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.transactions.TransactionId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CashReconciliationTest {

    private final AccountId account = AccountId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    @Test
    void start_createsAPendingSessionWithNoActualAmountYet() {

        CashReconciliation reconciliation = CashReconciliation.start(account, today, Money.of(2200), null);

        assertTrue(reconciliation.isPending());
        assertNull(reconciliation.getActualCashAmount());
        assertNull(reconciliation.getDifference());
        assertFalse(reconciliation.hasSnapshots());
    }

    @Test
    void start_rejectsNegativeExpectedCash() {

        assertThrows(IllegalArgumentException.class, () ->
                CashReconciliation.start(account, today, Money.of(-100), null));
    }

    @Test
    void addSnapshot_setsActualAmountAndDifference() {

        CashReconciliation reconciliation = CashReconciliation.start(account, today, Money.of(2200), null)
                .addSnapshot(Money.of(1940), "wallet count");

        assertTrue(reconciliation.hasSnapshots());
        assertEquals(Money.of(1940), reconciliation.getActualCashAmount());
        assertEquals(Money.of(-260), reconciliation.getDifference());
        assertTrue(reconciliation.requiresAdjustment());
        assertFalse(reconciliation.isBalanced());
    }

    @Test
    void addSnapshot_theLatestCountBecomesTheCurrentActual() {

        CashReconciliation reconciliation = CashReconciliation.start(account, today, Money.of(2200), null)
                .addSnapshot(Money.of(1940), "morning")
                .addSnapshot(Money.of(2000), "evening, found more cash");

        assertEquals(2, reconciliation.getSnapshots().size());
        assertEquals(Money.of(2000), reconciliation.getActualCashAmount());
        assertEquals(Money.of(-200), reconciliation.getDifference());
    }

    @Test
    void addSnapshot_rejectsOnACompletedReconciliation() {

        CashReconciliation completed = CashReconciliation.start(account, today, Money.of(2200), null)
                .addSnapshot(Money.of(2200), null)
                .complete();

        assertThrows(IllegalStateException.class, () -> completed.addSnapshot(Money.of(2100), null));
    }

    @Test
    void isBalanced_isTrueWhenActualMatchesExpected() {

        CashReconciliation reconciliation = CashReconciliation.start(account, today, Money.of(2200), null)
                .addSnapshot(Money.of(2200), null);

        assertTrue(reconciliation.isBalanced());
        assertFalse(reconciliation.requiresAdjustment());
    }

    @Test
    void complete_rejectsWithoutAnySnapshot() {

        CashReconciliation reconciliation = CashReconciliation.start(account, today, Money.of(2200), null);

        assertThrows(IllegalStateException.class, reconciliation::complete);
    }

    @Test
    void complete_rejectsAlreadyCompletedReconciliation() {

        CashReconciliation completed = CashReconciliation.start(account, today, Money.of(2200), null)
                .addSnapshot(Money.of(2200), null)
                .complete();

        assertThrows(IllegalStateException.class, completed::complete);
    }

    @Test
    void linkAdjustment_attachesTheTransactionId() {

        TransactionId adjustmentId = TransactionId.newId();

        CashReconciliation reconciliation = CashReconciliation.start(account, today, Money.of(2200), null)
                .addSnapshot(Money.of(1940), null)
                .complete()
                .linkAdjustment(adjustmentId);

        assertEquals(adjustmentId, reconciliation.getAdjustmentTransactionId());
    }

    @Test
    void linkAdjustment_rejectsWhenAlreadyLinked() {

        CashReconciliation reconciliation = CashReconciliation.start(account, today, Money.of(2200), null)
                .addSnapshot(Money.of(1940), null)
                .complete()
                .linkAdjustment(TransactionId.newId());

        assertThrows(IllegalStateException.class, () -> reconciliation.linkAdjustment(TransactionId.newId()));
    }
}
