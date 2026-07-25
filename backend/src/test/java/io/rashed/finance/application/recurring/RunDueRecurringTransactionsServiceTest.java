package io.rashed.finance.application.recurring;

import io.rashed.finance.common.enums.Frequency;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RunDueRecurringTransactionsServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final CategoryId categoryId = CategoryId.newId();
    private final LocalDate startDate = LocalDate.of(2026, 1, 1);
    private final LocalDate asOfDate = LocalDate.of(2026, 4, 1);

    private RecurringTransactionRepository repository;
    private GenerateRecurringTransactionNowService generateService;
    private RunDueRecurringTransactionsService service;

    @BeforeEach
    void setUp() {

        repository = mock(RecurringTransactionRepository.class);
        generateService = mock(GenerateRecurringTransactionNowService.class);
        service = new RunDueRecurringTransactionsService(repository, generateService);
    }

    @Test
    void execute_delegatesEachDueTemplateToGenerateAllDueOccurrences() {

        RecurringTransaction template = anExpense();
        when(repository.findDueForAutoGeneration(asOfDate)).thenReturn(List.of(template));

        RecurringTransaction caughtUp = template.markExecuted().markExecuted().markExecuted();
        when(generateService.generateAllDueOccurrences(template, asOfDate)).thenReturn(caughtUp);

        List<RecurringTransaction> results = service.execute(asOfDate);

        assertEquals(1, results.size());
        assertEquals(caughtUp, results.get(0));
        verify(generateService).generateAllDueOccurrences(eq(template), eq(asOfDate));
    }

    @Test
    void execute_returnsEmptyWhenNothingDue() {

        when(repository.findDueForAutoGeneration(asOfDate)).thenReturn(List.of());

        List<RecurringTransaction> results = service.execute(asOfDate);

        assertEquals(0, results.size());
    }

    private RecurringTransaction anExpense() {

        return RecurringTransaction.expense(
                "House Rent", accountId, categoryId, Money.of(10000), Frequency.MONTHLY, startDate, null, true, null, null);
    }
}
