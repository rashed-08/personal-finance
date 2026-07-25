package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;

import java.time.YearMonth;
import java.util.List;

public record MonthlyReportResult(

        YearMonth yearMonth,

        Money totalIncome,

        Money totalExpense,

        Money netCashFlow,

        List<CategoryBreakdown> expenseByCategory,

        List<CategoryBreakdown> incomeByCategory,

        MonthComparison comparisonToPreviousMonth

) {
}
