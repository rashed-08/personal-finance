package io.rashed.finance.application.transaction;

import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateTransactionServiceTest {

    private final LocalDate today = LocalDate.of(2026, 7, 25);
    private final AccountId account = AccountId.newId();
    private final AccountId otherAccount = AccountId.newId();
    private final CategoryId category = CategoryId.newId();
    private final SalaryCycleId salaryCycle = SalaryCycleId.newId();

    private TransactionRepository repository;
    private UpdateTransactionService service;

    @BeforeEach
    void setUp() {

        repository = mock(TransactionRepository.class);
        service = new UpdateTransactionService(repository);

        when(repository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void execute_appliesDescriptiveFieldsWithoutCreatingAdjustmentWhenAmountUnchanged() {

        Transaction existing = anExpense(Money.of(500));
        when(repository.findById(existing.getId())).thenReturn(Optional.of(existing));

        CategoryId newCategory = CategoryId.newId();
        UpdateTransactionCommand command = new UpdateTransactionCommand(
                existing.getId(), Money.of(500), newCategory, "updated", "notes");

        Transaction result = service.execute(command);

        assertEquals(newCategory, result.getCategoryId());
        assertEquals("updated", result.getDescription());
        verify(repository, times(1)).save(any(Transaction.class));
    }

    @Test
    void execute_rejectsUpdatingVoidedTransaction() {

        Transaction voided = anExpense(Money.of(500)).voidTransaction();
        when(repository.findById(voided.getId())).thenReturn(Optional.of(voided));

        UpdateTransactionCommand command = new UpdateTransactionCommand(
                voided.getId(), Money.of(500), category, "d", "n");

        assertThrows(IllegalStateException.class, () -> service.execute(command));
    }

    @Test
    void execute_rejectsUpdatingReversedTransaction() {

        Transaction reversed = anExpense(Money.of(500)).reverse();
        when(repository.findById(reversed.getId())).thenReturn(Optional.of(reversed));

        UpdateTransactionCommand command = new UpdateTransactionCommand(
                reversed.getId(), Money.of(500), category, "d", "n");

        assertThrows(IllegalStateException.class, () -> service.execute(command));
    }

    @Test
    void execute_rejectsAmountChangeOnTransfer() {

        Transaction transfer = Transaction.transfer(
                TransactionId.newId(), today, Money.of(1000), account, otherAccount, salaryCycle, "ATM");
        when(repository.findById(transfer.getId())).thenReturn(Optional.of(transfer));

        UpdateTransactionCommand command = new UpdateTransactionCommand(
                transfer.getId(), Money.of(1200), null, "d", "n");

        assertThrows(IllegalStateException.class, () -> service.execute(command));
    }

    @Test
    void execute_recordsDecreaseAdjustmentWhenExpenseAmountGrows() {

        Transaction existing = anExpense(Money.of(500));
        when(repository.findById(existing.getId())).thenReturn(Optional.of(existing));

        UpdateTransactionCommand command = new UpdateTransactionCommand(
                existing.getId(), Money.of(700), category, existing.getDescription(), null);

        service.execute(command);

        Transaction adjustment = capturedAdjustment();

        assertEquals(Money.of(200), adjustment.getAmount());
        assertEquals(account, adjustment.getFromAccountId());
        assertNull(adjustment.getToAccountId());
        assertEquals(AdjustmentReason.TRANSACTION_UPDATE, adjustment.getAdjustmentReason());
        assertEquals(existing.getId(), adjustment.getReferenceTransactionId());
    }

    @Test
    void execute_recordsIncreaseAdjustmentWhenExpenseAmountShrinks() {

        Transaction existing = anExpense(Money.of(500));
        when(repository.findById(existing.getId())).thenReturn(Optional.of(existing));

        UpdateTransactionCommand command = new UpdateTransactionCommand(
                existing.getId(), Money.of(300), category, existing.getDescription(), null);

        service.execute(command);

        Transaction adjustment = capturedAdjustment();

        assertEquals(Money.of(200), adjustment.getAmount());
        assertEquals(account, adjustment.getToAccountId());
        assertNull(adjustment.getFromAccountId());
    }

    @Test
    void execute_recordsIncreaseAdjustmentWhenIncomeAmountGrows() {

        Transaction existing = Transaction.income(
                TransactionId.newId(), today, Money.of(500), account, category, salaryCycle, "Salary");
        when(repository.findById(existing.getId())).thenReturn(Optional.of(existing));

        UpdateTransactionCommand command = new UpdateTransactionCommand(
                existing.getId(), Money.of(800), category, existing.getDescription(), null);

        service.execute(command);

        Transaction adjustment = capturedAdjustment();

        assertEquals(account, adjustment.getToAccountId());
        assertNull(adjustment.getFromAccountId());
    }

    private Transaction capturedAdjustment() {

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(repository, times(2)).save(captor.capture());

        return captor.getAllValues().get(1);
    }

    private Transaction anExpense(Money amount) {

        return Transaction.expense(
                TransactionId.newId(), today, amount, account, category, salaryCycle, "Groceries");
    }
}
