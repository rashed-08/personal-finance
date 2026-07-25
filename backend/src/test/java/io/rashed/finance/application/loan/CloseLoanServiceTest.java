package io.rashed.finance.application.loan;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanId;
import io.rashed.finance.domain.loans.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CloseLoanServiceTest {

    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private LoanRepository loanRepository;
    private CalculateLoanBalanceService calculateLoanBalanceService;
    private CloseLoanService service;

    @BeforeEach
    void setUp() {

        loanRepository = mock(LoanRepository.class);
        calculateLoanBalanceService = mock(CalculateLoanBalanceService.class);
        service = new CloseLoanService(loanRepository, calculateLoanBalanceService);

        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void execute_closesLoanWhenBalanceIsZero() {

        Loan loan = Loan.receivable("Rahim", Money.of(10000), today, null, null);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(calculateLoanBalanceService.execute(loan.getId())).thenReturn(Money.zero());

        Loan closed = service.execute(loan.getId());

        assertTrue(closed.isClosed());
    }

    @Test
    void execute_rejectsNonZeroBalance() {

        Loan loan = Loan.receivable("Rahim", Money.of(10000), today, null, null);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(calculateLoanBalanceService.execute(loan.getId())).thenReturn(Money.of(500));

        assertThrows(IllegalStateException.class, () -> service.execute(loan.getId()));
    }

    @Test
    void execute_rejectsUnknownLoan() {

        LoanId loanId = LoanId.newId();
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(loanId));
    }
}
