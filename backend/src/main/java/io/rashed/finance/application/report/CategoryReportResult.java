package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.categories.CategoryId;

import java.util.List;

public record CategoryReportResult(

        CategoryId categoryId,

        String categoryName,

        Money totalSpending,

        long transactionCount,

        List<MonthlyAmount> monthlyTrend,

        Money averagePerMonth

) {
}
