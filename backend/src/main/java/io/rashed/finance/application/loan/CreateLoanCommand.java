package io.rashed.finance.application.loan;

import java.time.LocalDate;

import io.rashed.finance.common.enums.LoanType;
import io.rashed.finance.common.valueobject.Money;

public record CreateLoanCommand(

        String name,

        LoanType loanType,

        Money principalAmount,

        LocalDate dueDate,

        String description

) {
}