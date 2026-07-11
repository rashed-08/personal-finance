package io.rashed.finance.application.reconciliation;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;

import java.time.LocalDate;

public record CreateReconciliationCommand(

        AccountId accountId,

        LocalDate reconciliationDate,

        Money expectedBalance,

        Money actualBalance,

        String description

) {
}