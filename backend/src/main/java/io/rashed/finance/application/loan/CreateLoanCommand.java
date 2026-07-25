package io.rashed.finance.application.loan;

import java.time.LocalDate;

import io.rashed.finance.common.enums.LoanType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;

public record CreateLoanCommand(

        String name,

        LoanType loanType,

        Money principalAmount,

        LocalDate startDate,

        LocalDate dueDate,

        AccountId accountId,

        SalaryCycleId salaryCycleId,

        String description

) {
}
