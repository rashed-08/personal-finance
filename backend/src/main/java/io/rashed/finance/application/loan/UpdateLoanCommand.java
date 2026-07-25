package io.rashed.finance.application.loan;

import io.rashed.finance.domain.loans.LoanId;

import java.time.LocalDate;

public record UpdateLoanCommand(

        LoanId loanId,

        String name,

        LocalDate dueDate,

        String description

) {
}
