package io.rashed.finance.application.report;

import io.rashed.finance.common.enums.Frequency;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionExecution;
import io.rashed.finance.domain.recurring.RecurringTransactionExecutionRepository;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;
import io.rashed.finance.domain.transactions.TransactionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetRecurringTransactionReportServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final CategoryId categoryId = CategoryId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private RecurringTransactionRepository recurringTransactionRepository;
    private RecurringTransactionExecutionRepository executionRepository;
    private GetRecurringTransactionReportService service;

    @BeforeEach
    void setUp() {

        recurringTransactionRepository = mock(RecurringTransactionRepository.class);
        executionRepository = mock(RecurringTransactionExecutionRepository.class);
        service = new GetRecurringTransactionReportService(recurringTransactionRepository, executionRepository);
    }

    @Test
    void execute_countsGeneratedAndSkippedExecutions() {

        RecurringTransaction template = RecurringTransaction.expense(
                "Rent", accountId, categoryId, Money.of(1000), Frequency.MONTHLY, today, null, true, null, null);

        when(recurringTransactionRepository.findActive()).thenReturn(List.of(template));

        RecurringTransactionExecution generated = RecurringTransactionExecution.generated(
                template.getId(), today, TransactionId.newId());
        RecurringTransactionExecution skipped = RecurringTransactionExecution.skipped(
                template.getId(), today.plusMonths(1), "Account closed for the month");

        when(executionRepository.findByRecurringTransactionId(template.getId()))
                .thenReturn(List.of(generated, skipped));

        List<RecurringTransactionReportLine> result = service.execute(true);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).generatedCount());
        assertEquals(1, result.get(0).skippedCount());
        assertEquals(template.getName(), result.get(0).name());
    }
}
