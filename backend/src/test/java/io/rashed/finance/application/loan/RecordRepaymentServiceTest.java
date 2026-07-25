package io.rashed.finance.application.loan;

import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanRepository;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecordRepaymentServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final SalaryCycleId salaryCycleId = SalaryCycleId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private LoanRepository loanRepository;
    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private SalaryCycleRepository salaryCycleRepository;
    private CalculateLoanBalanceService calculateLoanBalanceService;
    private RecordRepaymentService service;

    @BeforeEach
    void setUp() {

        loanRepository = mock(LoanRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        accountRepository = mock(AccountRepository.class);
        salaryCycleRepository = mock(SalaryCycleRepository.class);
        calculateLoanBalanceService = mock(CalculateLoanBalanceService.class);

        service = new RecordRepaymentService(
                loanRepository, transactionRepository, accountRepository, salaryCycleRepository, calculateLoanBalanceService);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void execute_receivableLoanPostsCollectionToAccount() {

        Loan loan = Loan.receivable("Rahim", Money.of(10000), today, null, null);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(calculateLoanBalanceService.execute(loan.getId())).thenReturn(Money.of(10000));
        givenActiveAccount();
        givenSalaryCycleExists();

        RecordRepaymentCommand command = new RecordRepaymentCommand(
                loan.getId(), accountId, Money.of(3000), today, salaryCycleId, "Collection");

        service.execute(command);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction collection = captor.getValue();
        assertEquals(accountId, collection.getToAccountId());
        assertEquals(null, collection.getFromAccountId());
        assertEquals(loan.getId(), collection.getLoanId());
    }

    @Test
    void execute_payableLoanPostsRepaymentFromAccount() {

        Loan loan = Loan.payable("Brother", Money.of(25000), today, null, null);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(calculateLoanBalanceService.execute(loan.getId())).thenReturn(Money.of(25000));
        givenActiveAccount();
        givenSalaryCycleExists();

        RecordRepaymentCommand command = new RecordRepaymentCommand(
                loan.getId(), accountId, Money.of(5000), today, salaryCycleId, "Repayment");

        service.execute(command);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction repayment = captor.getValue();
        assertEquals(accountId, repayment.getFromAccountId());
        assertEquals(null, repayment.getToAccountId());
    }

    @Test
    void execute_rejectsUnknownLoan() {

        when(loanRepository.findById(any())).thenReturn(Optional.empty());

        RecordRepaymentCommand command = new RecordRepaymentCommand(
                io.rashed.finance.domain.loans.LoanId.newId(), accountId, Money.of(1000), today, salaryCycleId, null);

        assertThrows(ResourceNotFoundException.class, () -> service.execute(command));
    }

    @Test
    void execute_rejectsClosedLoan() {

        Loan loan = Loan.receivable("Rahim", Money.of(10000), today, null, null).close();
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        RecordRepaymentCommand command = new RecordRepaymentCommand(
                loan.getId(), accountId, Money.of(1000), today, salaryCycleId, null);

        assertThrows(IllegalStateException.class, () -> service.execute(command));
    }

    @Test
    void execute_rejectsAmountExceedingOutstandingBalance() {

        Loan loan = Loan.receivable("Rahim", Money.of(10000), today, null, null);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(calculateLoanBalanceService.execute(loan.getId())).thenReturn(Money.of(2000));
        givenActiveAccount();
        givenSalaryCycleExists();

        RecordRepaymentCommand command = new RecordRepaymentCommand(
                loan.getId(), accountId, Money.of(3000), today, salaryCycleId, null);

        assertThrows(TransactionValidationException.class, () -> service.execute(command));
    }

    @Test
    void execute_rejectsInactiveAccount() {

        Loan loan = Loan.receivable("Rahim", Money.of(10000), today, null, null);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        Account inactiveAccount = Account.create("Cash", AccountType.CASH, Money.zero(), null).deactivate();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(inactiveAccount));

        RecordRepaymentCommand command = new RecordRepaymentCommand(
                loan.getId(), accountId, Money.of(1000), today, salaryCycleId, null);

        assertThrows(TransactionValidationException.class, () -> service.execute(command));
    }

    @Test
    void execute_rejectsUnknownSalaryCycle() {

        Loan loan = Loan.receivable("Rahim", Money.of(10000), today, null, null);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        givenActiveAccount();
        when(salaryCycleRepository.findById(salaryCycleId)).thenReturn(Optional.empty());

        RecordRepaymentCommand command = new RecordRepaymentCommand(
                loan.getId(), accountId, Money.of(1000), today, salaryCycleId, null);

        assertThrows(ResourceNotFoundException.class, () -> service.execute(command));
    }

    private void givenActiveAccount() {

        Account account = Account.create("Cash", AccountType.CASH, Money.zero(), null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    }

    private void givenSalaryCycleExists() {

        SalaryCycle salaryCycle = SalaryCycle.create(
                "July 2026", today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()), today, null);

        when(salaryCycleRepository.findById(salaryCycleId)).thenReturn(Optional.of(salaryCycle));
    }
}
