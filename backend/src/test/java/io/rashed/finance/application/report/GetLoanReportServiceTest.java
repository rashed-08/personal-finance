package io.rashed.finance.application.report;

import io.rashed.finance.application.loan.CalculateLoanBalanceService;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanId;
import io.rashed.finance.domain.loans.LoanRepository;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetLoanReportServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final SalaryCycleId salaryCycleId = SalaryCycleId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private LoanRepository loanRepository;
    private TransactionRepository transactionRepository;
    private CalculateLoanBalanceService calculateLoanBalanceService;
    private GetLoanReportService service;

    @BeforeEach
    void setUp() {

        loanRepository = mock(LoanRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        calculateLoanBalanceService = mock(CalculateLoanBalanceService.class);
        service = new GetLoanReportService(loanRepository, transactionRepository, calculateLoanBalanceService);
    }

    @Test
    void executeOne_rejectsUnknownLoan() {

        LoanId loanId = LoanId.newId();
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.executeOne(loanId));
    }

    @Test
    void executeOne_buildsPaymentHistoryForReceivableLoan() {

        Loan loan = Loan.receivable("Friend Loan", Money.of(5000), today, null, null);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(calculateLoanBalanceService.execute(loan.getId())).thenReturn(Money.of(3000));

        Transaction disbursement = Transaction.loanTransfer(
                TransactionId.newId(), today, Money.of(5000), accountId, null, loan.getId(), salaryCycleId, null);
        Transaction repayment = Transaction.loanTransfer(
                TransactionId.newId(), today.plusDays(1), Money.of(2000), null, accountId, loan.getId(), salaryCycleId, null);

        Page<Transaction> page = new PageImpl<>(List.of(disbursement, repayment));
        when(transactionRepository.find(any(), any(Pageable.class))).thenReturn(page);

        LoanReportLine line = service.executeOne(loan.getId());

        assertEquals(Money.of(5000), line.principalAmount());
        assertEquals(Money.of(2000), line.paidAmount());
        assertEquals(Money.of(3000), line.remainingAmount());
        assertEquals(1, line.paymentHistory().size());
        assertEquals(Money.of(2000), line.paymentHistory().get(0).amount());
    }

    @Test
    void execute_listsOnlyActiveLoansWhenRequested() {

        Loan loan = Loan.payable("Car Loan", Money.of(10000), today, null, null);
        when(loanRepository.findActiveLoans()).thenReturn(List.of(loan));
        when(calculateLoanBalanceService.execute(loan.getId())).thenReturn(Money.of(10000));
        when(transactionRepository.find(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        List<LoanReportLine> lines = service.execute(true);

        assertEquals(1, lines.size());
        assertEquals(Money.zero(), lines.get(0).paidAmount());
    }
}
