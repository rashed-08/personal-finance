package io.rashed.finance.application.loan;

import java.util.Objects;

import io.rashed.finance.common.enums.LoanType;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanRepository;

public class CreateLoanService {

    private final LoanRepository repository;

    public CreateLoanService(LoanRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Loan create(CreateLoanCommand command) {

        Objects.requireNonNull(command);

        Loan loan;

        if (command.loanType() == LoanType.GIVEN) {

            loan = Loan.given(
                    command.name(),
                    command.principalAmount(),
                    command.dueDate(),
                    command.description()
            );

        } else {

            loan = Loan.received(
                    command.name(),
                    command.principalAmount(),
                    command.dueDate(),
                    command.description()
            );
        }

        return repository.save(loan);
    }
}