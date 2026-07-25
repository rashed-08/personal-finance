package io.rashed.finance.application.loan;

import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.enums.LoanType;
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

class CreateLoanServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final SalaryCycleId salaryCycleId = SalaryCycleId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private LoanRepository loanRepository;
    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private SalaryCycleRepository salaryCycleRepository;
    private CreateLoanService service;

    @BeforeEach
    void setUp() {

        loanRepository = mock(LoanRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        accountRepository = mock(AccountRepository.class);
        salaryCycleRepository = mock(SalaryCycleRepository.class);

        service = new CreateLoanService(loanRepository, transactionRepository, accountRepository, salaryCycleRepository);

        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void create_receivableLoanPostsDisbursementFromAccount() {

        givenActiveAccount();
        givenSalaryCycleExists();

        CreateLoanCommand command = new CreateLoanCommand(
                "Rahim", LoanType.RECEIVABLE, Money.of(10000), today, null, accountId, salaryCycleId, "Loan given");

        Loan loan = service.create(command);

        assertEquals(LoanType.RECEIVABLE, loan.getLoanType());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction disbursement = captor.getValue();
        assertEquals(accountId, disbursement.getFromAccountId());
        assertEquals(null, disbursement.getToAccountId());
        assertEquals(loan.getId(), disbursement.getLoanId());
    }

    @Test
    void create_payableLoanPostsReceiptToAccount() {

        givenActiveAccount();
        givenSalaryCycleExists();

        CreateLoanCommand command = new CreateLoanCommand(
                "Brother", LoanType.PAYABLE, Money.of(25000), today, null, accountId, salaryCycleId, "Loan received");

        Loan loan = service.create(command);

        assertEquals(LoanType.PAYABLE, loan.getLoanType());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction receipt = captor.getValue();
        assertEquals(accountId, receipt.getToAccountId());
        assertEquals(null, receipt.getFromAccountId());
        assertEquals(loan.getId(), receipt.getLoanId());
    }

    @Test
    void create_rejectsUnknownAccount() {

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        givenSalaryCycleExists();

        CreateLoanCommand command = new CreateLoanCommand(
                "Rahim", LoanType.RECEIVABLE, Money.of(10000), today, null, accountId, salaryCycleId, null);

        assertThrows(ResourceNotFoundException.class, () -> service.create(command));
    }

    @Test
    void create_rejectsInactiveAccount() {

        Account account = Account.create("Cash", AccountType.CASH, Money.zero(), null).deactivate();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        givenSalaryCycleExists();

        CreateLoanCommand command = new CreateLoanCommand(
                "Rahim", LoanType.RECEIVABLE, Money.of(10000), today, null, accountId, salaryCycleId, null);

        assertThrows(TransactionValidationException.class, () -> service.create(command));
    }

    @Test
    void create_rejectsUnknownSalaryCycle() {

        givenActiveAccount();
        when(salaryCycleRepository.findById(salaryCycleId)).thenReturn(Optional.empty());

        CreateLoanCommand command = new CreateLoanCommand(
                "Rahim", LoanType.RECEIVABLE, Money.of(10000), today, null, accountId, salaryCycleId, null);

        assertThrows(ResourceNotFoundException.class, () -> service.create(command));
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
