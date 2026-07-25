package io.rashed.finance.application.loan;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanId;
import io.rashed.finance.domain.loans.LoanRepository;

/** A loan may only be closed once its outstanding balance is zero. */
@Service
public class CloseLoanService {

    private final LoanRepository loanRepository;
    private final CalculateLoanBalanceService calculateLoanBalanceService;

    public CloseLoanService(LoanRepository loanRepository, CalculateLoanBalanceService calculateLoanBalanceService) {
        this.loanRepository = Objects.requireNonNull(loanRepository);
        this.calculateLoanBalanceService = Objects.requireNonNull(calculateLoanBalanceService);
    }

    public Loan execute(LoanId id) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id.getValue()));

        if (!calculateLoanBalanceService.execute(id).isZero()) {
            throw new IllegalStateException("Loan cannot be closed while it has a non-zero outstanding balance.");
        }

        return loanRepository.save(loan.close());
    }
}
