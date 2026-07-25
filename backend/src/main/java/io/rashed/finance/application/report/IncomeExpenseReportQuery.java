package io.rashed.finance.application.report;

import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;

import java.time.LocalDate;

public record IncomeExpenseReportQuery(

        TransactionType transactionType,

        LocalDate fromDate,

        LocalDate toDate,

        SalaryCycleId salaryCycleId,

        AccountId accountId,

        CategoryId categoryId

) {
}
