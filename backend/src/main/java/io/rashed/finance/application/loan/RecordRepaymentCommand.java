package io.rashed.finance.application.loan;

import java.time.LocalDate;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.loans.LoanId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;

public record RecordRepaymentCommand(

        LoanId loanId,

        AccountId accountId,

        Money amount,

        LocalDate paymentDate,

        SalaryCycleId salaryCycleId,

        String description

) {
}
