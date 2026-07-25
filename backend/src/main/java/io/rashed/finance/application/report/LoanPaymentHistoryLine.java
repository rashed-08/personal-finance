package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.transactions.TransactionId;

import java.time.LocalDate;

public record LoanPaymentHistoryLine(

        TransactionId transactionId,

        LocalDate date,

        Money amount,

        String description

) {
}
