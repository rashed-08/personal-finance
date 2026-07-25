package io.rashed.finance.application.loan;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanRepository;

@Service
public class UpdateLoanService {

    private final LoanRepository repository;

    public UpdateLoanService(LoanRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Loan execute(UpdateLoanCommand command) {

        Objects.requireNonNull(command, "Command cannot be null.");

        Loan loan = repository.findById(command.loanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + command.loanId().getValue()));

        Loan updated = loan
                .rename(command.name())
                .changeDueDate(command.dueDate())
                .changeDescription(command.description());

        return repository.save(updated);
    }
}
