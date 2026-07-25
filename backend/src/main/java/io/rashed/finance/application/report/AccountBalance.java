package io.rashed.finance.application.report;

import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;

public record AccountBalance(

        AccountId accountId,

        String accountName,

        AccountType accountType,

        Money balance

) {
}
