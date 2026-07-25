package io.rashed.finance.application.loan;

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

class CalculateLoanBalanceServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final SalaryCycleId salaryCycleId = SalaryCycleId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private LoanRepository loanRepository;
    private TransactionRepository transactionRepository;
    private CalculateLoanBalanceService service;

    @BeforeEach
    void setUp() {

        loanRepository = mock(LoanRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        service = new CalculateLoanBalanceService(loanRepository, transactionRepository);
    }

    @Test
    void execute_rejectsUnknownLoan() {

        LoanId loanId = LoanId.newId();
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(loanId));
    }

    @Test
    void execute_receivableLoanEqualsPrincipalMinusCollections() {

        Loan loan = Loan.receivable("Rahim", Money.of(10000), today, null, null);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        // Disbursement: fromAccountId set (money leaves) - excluded by direction filter.
        Transaction disbursement = Transaction.loanTransfer(
                TransactionId.newId(), today, Money.of(10000), accountId, null, loan.getId(), salaryCycleId, null);

        // Collection: toAccountId set (money returns) - counted.
        Transaction collection = Transaction.loanTransfer(
                TransactionId.newId(), today, Money.of(3000), null, accountId, loan.getId(), salaryCycleId, null);

        givenTransactions(List.of(disbursement, collection));

        assertEquals(Money.of(7000), service.execute(loan.getId()));
    }

    @Test
    void execute_payableLoanEqualsPrincipalMinusRepayments() {

        Loan loan = Loan.payable("Brother", Money.of(25000), today, null, null);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        // Receipt: toAccountId set (money enters) - excluded by direction filter.
        Transaction receipt = Transaction.loanTransfer(
                TransactionId.newId(), today, Money.of(25000), null, accountId, loan.getId(), salaryCycleId, null);

        // Repayment: fromAccountId set (money leaves) - counted.
        Transaction repayment = Transaction.loanTransfer(
                TransactionId.newId(), today, Money.of(5000), accountId, null, loan.getId(), salaryCycleId, null);

        givenTransactions(List.of(receipt, repayment));

        assertEquals(Money.of(20000), service.execute(loan.getId()));
    }

    @Test
    void execute_isFullPrincipalWithNoRepayments() {

        Loan loan = Loan.receivable("Rahim", Money.of(10000), today, null, null);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        givenTransactions(List.of());

        assertEquals(Money.of(10000), service.execute(loan.getId()));
    }

    private void givenTransactions(List<Transaction> transactions) {

        Page<Transaction> page = new PageImpl<>(transactions);

        when(transactionRepository.find(any(), any(Pageable.class))).thenReturn(page);
    }
}
