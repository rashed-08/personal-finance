package io.rashed.finance.application.report;

import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetMonthlyReportServiceTest {

    private final YearMonth july = YearMonth.of(2026, 7);

    private GetIncomeExpenseReportService getIncomeExpenseReportService;
    private GetMonthlyReportService service;

    @BeforeEach
    void setUp() {

        getIncomeExpenseReportService = mock(GetIncomeExpenseReportService.class);
        service = new GetMonthlyReportService(getIncomeExpenseReportService);
    }

    @Test
    void execute_computesNetCashFlowAndComparisonToPreviousMonth() {

        when(getIncomeExpenseReportService.execute(any())).thenAnswer(invocation -> {

            IncomeExpenseReportQuery query = invocation.getArgument(0);
            boolean isJuly = july.atDay(1).equals(query.fromDate());

            if (query.transactionType() == TransactionType.INCOME) {
                Money total = isJuly ? Money.of(5000) : Money.of(4000);
                return new IncomeExpenseReportResult(TransactionType.INCOME, total, 1, List.of(), List.of());
            }

            Money total = isJuly ? Money.of(3000) : Money.of(3500);
            return new IncomeExpenseReportResult(TransactionType.EXPENSE, total, 1, List.of(), List.of());
        });

        MonthlyReportResult result = service.execute(july);

        assertEquals(Money.of(5000), result.totalIncome());
        assertEquals(Money.of(3000), result.totalExpense());
        assertEquals(Money.of(2000), result.netCashFlow());
        assertEquals(Money.of(5000), result.comparisonToPreviousMonth().currentIncome());
        assertEquals(Money.of(4000), result.comparisonToPreviousMonth().previousIncome());
        assertEquals(Money.of(3000), result.comparisonToPreviousMonth().currentExpense());
        assertEquals(Money.of(3500), result.comparisonToPreviousMonth().previousExpense());
    }
}
