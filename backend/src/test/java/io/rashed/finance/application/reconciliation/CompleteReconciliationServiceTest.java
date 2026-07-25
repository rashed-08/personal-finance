package io.rashed.finance.application.reconciliation;

import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationId;
import io.rashed.finance.domain.reconciliation.CashReconciliationRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompleteReconciliationServiceTest {

    private final LocalDate today = LocalDate.of(2026, 7, 25);
    private final AccountId account = AccountId.newId();

    private CashReconciliationRepository repository;
    private TransactionRepository transactionRepository;
    private CompleteReconciliationService service;

    @BeforeEach
    void setUp() {

        repository = mock(CashReconciliationRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        service = new CompleteReconciliationService(repository, transactionRepository);

        when(repository.save(any(CashReconciliation.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void execute_createsADecreaseAdjustmentWhenCashIsMissing() {

        CashReconciliation reconciliation = CashReconciliation.start(account, today, Money.of(2200), null)
                .addSnapshot(Money.of(1940), null);
        when(repository.findById(reconciliation.getId())).thenReturn(Optional.of(reconciliation));

        CashReconciliation completed = service.execute(reconciliation.getId());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction adjustment = captor.getValue();
        assertEquals(Money.of(260), adjustment.getAmount());
        assertEquals(account, adjustment.getFromAccountId());
        assertNull(adjustment.getToAccountId());
        assertEquals(AdjustmentReason.CASH_RECONCILIATION, adjustment.getAdjustmentReason());
        assertEquals(adjustment.getId(), completed.getAdjustmentTransactionId());
    }

    @Test
    void execute_createsAnIncreaseAdjustmentWhenExtraCashIsFound() {

        CashReconciliation reconciliation = CashReconciliation.start(account, today, Money.of(2200), null)
                .addSnapshot(Money.of(2500), null);
        when(repository.findById(reconciliation.getId())).thenReturn(Optional.of(reconciliation));

        service.execute(reconciliation.getId());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction adjustment = captor.getValue();
        assertEquals(Money.of(300), adjustment.getAmount());
        assertEquals(account, adjustment.getToAccountId());
        assertNull(adjustment.getFromAccountId());
    }

    @Test
    void execute_createsNoAdjustmentWhenPerfectlyBalanced() {

        CashReconciliation reconciliation = CashReconciliation.start(account, today, Money.of(2200), null)
                .addSnapshot(Money.of(2200), null);
        when(repository.findById(reconciliation.getId())).thenReturn(Optional.of(reconciliation));

        CashReconciliation completed = service.execute(reconciliation.getId());

        verify(transactionRepository, never()).save(any(Transaction.class));
        assertNull(completed.getAdjustmentTransactionId());
        assertEquals(true, completed.isCompleted());
    }

    @Test
    void execute_rejectsUnknownReconciliation() {

        CashReconciliationId id = CashReconciliationId.newId();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(id));
    }
}
