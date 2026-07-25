package io.rashed.finance.application.loan;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanId;
import io.rashed.finance.domain.loans.LoanRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class GetLoanService {

    private final LoanRepository repository;

    public GetLoanService(LoanRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Loan execute(LoanId id) {

        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id.getValue()));
    }
}
