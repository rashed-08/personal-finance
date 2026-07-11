package io.rashed.finance.application.transaction;

import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;

import java.time.LocalDate;

public record CreateTransactionCommand(

        TransactionType transactionType,

        LocalDate transactionDate,

        Money amount,

        String description,

        String notes,

        AccountId fromAccountId,

        AccountId toAccountId,

        CategoryId categoryId,

        SalaryCycleId salaryCycleId,

        String referenceNumber,

        String migrationBatchId,

        String reconciliationBatchId,

        AdjustmentReason adjustmentReason

) {
}