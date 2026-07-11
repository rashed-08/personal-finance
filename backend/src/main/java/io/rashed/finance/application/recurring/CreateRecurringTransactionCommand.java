package io.rashed.finance.application.recurring;

import io.rashed.finance.common.enums.Frequency;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;

import java.time.LocalDate;

public record CreateRecurringTransactionCommand(

        TransactionType transactionType,

        AccountId fromAccountId,

        AccountId toAccountId,

        CategoryId categoryId,

        Money amount,

        Frequency frequency,

        LocalDate startDate,

        LocalDate endDate,

        String description
) {
}