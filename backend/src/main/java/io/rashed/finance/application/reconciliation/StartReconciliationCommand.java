package io.rashed.finance.application.reconciliation;

import io.rashed.finance.domain.accounts.AccountId;

import java.time.LocalDate;

public record StartReconciliationCommand(

        AccountId accountId,

        LocalDate reconciliationDate,

        String notes

) {
}
