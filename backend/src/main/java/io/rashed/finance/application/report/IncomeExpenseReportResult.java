package io.rashed.finance.application.report;

import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;

import java.util.List;

public record IncomeExpenseReportResult(

        TransactionType transactionType,

        Money total,

        long transactionCount,

        List<CategoryBreakdown> byCategory,

        List<DateBucket> byDate

) {
}
