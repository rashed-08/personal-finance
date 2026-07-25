package io.rashed.finance.api.dto.report;

import io.rashed.finance.common.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public record IncomeExpenseReportResponse(

        TransactionType transactionType,

        BigDecimal total,

        long transactionCount,

        List<CategoryBreakdownResponse> byCategory,

        List<DateBucketResponse> byDate

) {
}
