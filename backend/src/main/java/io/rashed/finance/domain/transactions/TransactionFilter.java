package io.rashed.finance.domain.transactions;

import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;

import java.time.LocalDate;

public record TransactionFilter(

        LocalDate fromDate,

        LocalDate toDate,

        TransactionType transactionType,

        TransactionStatus transactionStatus,

        AccountId accountId,

        CategoryId categoryId,

        SalaryCycleId salaryCycleId,

        FundId fundId

) {
}