package io.rashed.finance.api.dto.report;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CategoryReportResponse(

        UUID categoryId,

        String categoryName,

        BigDecimal totalSpending,

        long transactionCount,

        List<MonthlyAmountResponse> monthlyTrend,

        BigDecimal averagePerMonth

) {
}
