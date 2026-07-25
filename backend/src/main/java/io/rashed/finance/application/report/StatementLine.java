package io.rashed.finance.application.report;

import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.transactions.TransactionId;

import java.time.LocalDate;

public record StatementLine(

        TransactionId transactionId,

        LocalDate transactionDate,

        String description,

        TransactionType transactionType,

        Money signedAmount,

        Money runningBalance

) {
}
