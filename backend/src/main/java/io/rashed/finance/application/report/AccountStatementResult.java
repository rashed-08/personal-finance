package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;

import java.util.List;

public record AccountStatementResult(

        AccountId accountId,

        String accountName,

        Money openingBalance,

        List<StatementLine> lines,

        Money endingBalance

) {
}
