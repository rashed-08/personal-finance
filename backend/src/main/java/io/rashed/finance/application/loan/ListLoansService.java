package io.rashed.finance.application.loan;

import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ListLoansService {

    private final LoanRepository repository;

    public ListLoansService(LoanRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<Loan> execute(boolean activeOnly) {

        return activeOnly ? repository.findActiveLoans() : repository.findAll();
    }
}
