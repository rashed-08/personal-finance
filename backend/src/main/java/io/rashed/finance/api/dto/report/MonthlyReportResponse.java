package io.rashed.finance.api.dto.report;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MonthlyReportResponse(

        YearMonth yearMonth,

        BigDecimal totalIncome,

        BigDecimal totalExpense,

        BigDecimal netCashFlow,

        List<CategoryBreakdownResponse> expenseByCategory,

        List<CategoryBreakdownResponse> incomeByCategory,

        MonthComparisonResponse comparisonToPreviousMonth

) {
}
