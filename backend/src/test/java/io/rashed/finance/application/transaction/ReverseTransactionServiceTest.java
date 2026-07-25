package io.rashed.finance.application.transaction;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReverseTransactionServiceTest {

    private final LocalDate today = LocalDate.of(2026, 7, 25);
    private TransactionRepository repository;
    private ReverseTransactionService service;

    @BeforeEach
    void setUp() {

        repository = mock(TransactionRepository.class);
        service = new ReverseTransactionService(repository);

        when(repository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void execute_reversesAPostedTransaction() {

        Transaction expense = anExpense();
        when(repository.findById(expense.getId())).thenReturn(Optional.of(expense));

        Transaction result = service.execute(expense.getId());

        assertTrue(result.isReversed());
    }

    @Test
    void execute_rejectsUnknownTransaction() {

        TransactionId id = TransactionId.newId();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(id));
    }

    @Test
    void execute_rejectsVoidedTransaction() {

        Transaction voided = anExpense().voidTransaction();
        when(repository.findById(voided.getId())).thenReturn(Optional.of(voided));

        assertThrows(IllegalStateException.class, () -> service.execute(voided.getId()));
    }

    private Transaction anExpense() {

        return Transaction.expense(
                TransactionId.newId(), today, Money.of(500), AccountId.newId(),
                CategoryId.newId(), SalaryCycleId.newId(), "Groceries");
    }
}
