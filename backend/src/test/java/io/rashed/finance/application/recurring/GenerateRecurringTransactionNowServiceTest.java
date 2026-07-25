package io.rashed.finance.application.recurring;

import io.rashed.finance.application.transaction.CreateTransactionCommand;
import io.rashed.finance.application.transaction.CreateTransactionService;
import io.rashed.finance.common.enums.Frequency;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionExecution;
import io.rashed.finance.domain.recurring.RecurringTransactionExecutionRepository;
import io.rashed.finance.domain.recurring.RecurringTransactionId;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateRecurringTransactionNowServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final CategoryId categoryId = CategoryId.newId();
    private final LocalDate startDate = LocalDate.of(2026, 1, 1);

    private RecurringTransactionRepository recurringTransactionRepository;
    private RecurringTransactionExecutionRepository executionRepository;
    private SalaryCycleRepository salaryCycleRepository;
    private CreateTransactionService createTransactionService;
    private GenerateRecurringTransactionNowService service;

    @BeforeEach
    void setUp() {

        recurringTransactionRepository = mock(RecurringTransactionRepository.class);
        executionRepository = mock(RecurringTransactionExecutionRepository.class);
        salaryCycleRepository = mock(SalaryCycleRepository.class);
        createTransactionService = mock(CreateTransactionService.class);

        service = new GenerateRecurringTransactionNowService(
                recurringTransactionRepository, executionRepository, salaryCycleRepository, createTransactionService);

        when(recurringTransactionRepository.save(any(RecurringTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(executionRepository.save(any(RecurringTransactionExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void execute_generatesTransactionWhenSalaryCycleOpen() {

        RecurringTransaction template = anExpense();
        when(recurringTransactionRepository.findById(template.getId())).thenReturn(Optional.of(template));

        SalaryCycle openCycle = SalaryCycle.create(
                "July 2026", startDate.withDayOfMonth(1), startDate.withDayOfMonth(startDate.lengthOfMonth()), startDate, null);
        when(salaryCycleRepository.findOpen()).thenReturn(Optional.of(openCycle));

        Transaction created = Transaction.expense(
                TransactionId.newId(), startDate, Money.of(10000), accountId, categoryId, openCycle.getId(), null);
        when(createTransactionService.execute(any(CreateTransactionCommand.class))).thenReturn(created);

        RecurringTransaction result = service.execute(template.getId());

        assertEquals(Frequency.MONTHLY.advance(startDate), result.getNextExecutionDate());
        assertEquals(startDate, result.getLastExecutionDate());

        ArgumentCaptor<RecurringTransactionExecution> captor = ArgumentCaptor.forClass(RecurringTransactionExecution.class);
        verify(executionRepository).save(captor.capture());

        RecurringTransactionExecution execution = captor.getValue();
        assertEquals(true, execution.isGenerated());
        assertEquals(created.getId(), execution.getTransactionId());
        assertEquals(startDate, execution.getScheduledDate());
    }

    @Test
    void execute_skipsWhenNoSalaryCycleOpen() {

        RecurringTransaction template = anExpense();
        when(recurringTransactionRepository.findById(template.getId())).thenReturn(Optional.of(template));
        when(salaryCycleRepository.findOpen()).thenReturn(Optional.empty());

        RecurringTransaction result = service.execute(template.getId());

        assertEquals(Frequency.MONTHLY.advance(startDate), result.getNextExecutionDate());

        ArgumentCaptor<RecurringTransactionExecution> captor = ArgumentCaptor.forClass(RecurringTransactionExecution.class);
        verify(executionRepository).save(captor.capture());

        assertEquals(true, captor.getValue().isSkipped());
    }

    @Test
    void execute_skipsWhenTransactionCreationFails() {

        RecurringTransaction template = anExpense();
        when(recurringTransactionRepository.findById(template.getId())).thenReturn(Optional.of(template));

        SalaryCycle openCycle = SalaryCycle.create(
                "July 2026", startDate.withDayOfMonth(1), startDate.withDayOfMonth(startDate.lengthOfMonth()), startDate, null);
        when(salaryCycleRepository.findOpen()).thenReturn(Optional.of(openCycle));

        when(createTransactionService.execute(any(CreateTransactionCommand.class)))
                .thenThrow(new TransactionValidationException("Source account is not active."));

        RecurringTransaction result = service.execute(template.getId());

        assertEquals(Frequency.MONTHLY.advance(startDate), result.getNextExecutionDate());

        ArgumentCaptor<RecurringTransactionExecution> captor = ArgumentCaptor.forClass(RecurringTransactionExecution.class);
        verify(executionRepository).save(captor.capture());

        assertEquals(true, captor.getValue().isSkipped());
        assertEquals("Source account is not active.", captor.getValue().getReason());
    }

    @Test
    void execute_rejectsUnknownTemplate() {

        RecurringTransactionId id = RecurringTransactionId.newId();
        when(recurringTransactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(id));
    }

    @Test
    void execute_rejectsInactiveTemplate() {

        RecurringTransaction inactive = anExpense().deactivate();
        when(recurringTransactionRepository.findById(inactive.getId())).thenReturn(Optional.of(inactive));

        assertThrows(IllegalStateException.class, () -> service.execute(inactive.getId()));
    }

    private RecurringTransaction anExpense() {

        return RecurringTransaction.expense(
                "House Rent", accountId, categoryId, Money.of(10000), Frequency.MONTHLY, startDate, null, false, null, null);
    }
}
