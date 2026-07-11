package io.rashed.finance.domain.loans;

import java.util.List;
import java.util.Optional;

public interface LoanRepository {

    Loan save(Loan loan);

    Optional<Loan> findById(LoanId id);

    List<Loan> findAll();

    List<Loan> findActiveLoans();

    boolean existsById(LoanId id);

    void deleteById(LoanId id);
}