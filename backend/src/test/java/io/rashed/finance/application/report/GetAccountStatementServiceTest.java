package io.rashed.finance.application.report;

import io.rashed.finance.application.account.CalculateAccountBalanceService;
import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.categories.CategoryId;
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

class GetAccountStatementServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final CategoryId categoryId = CategoryId.newId();
    private final SalaryCycleId salaryCycleId = SalaryCycleId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private CalculateAccountBalanceService calculateAccountBalanceService;
    private GetAccountStatementService service;

    @BeforeEach
    void setUp() {

        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        calculateAccountBalanceService = mock(CalculateAccountBalanceService.class);
        service = new GetAccountStatementService(accountRepository, transactionRepository, calculateAccountBalanceService);
    }

    @Test
    void execute_rejectsUnknownAccount() {

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(accountId, null, null));
    }

    @Test
    void execute_computesRunningBalanceFromOpeningBalance() {

        Account account = Account.create("Cash", AccountType.CASH, Money.zero(), null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(calculateAccountBalanceService.execute(accountId, today.minusDays(1))).thenReturn(Money.of(1000));

        Transaction income = Transaction.income(
                TransactionId.newId(), today, Money.of(500), accountId, categoryId, salaryCycleId, null);
        Transaction expense = Transaction.expense(
                TransactionId.newId(), today.plusDays(1), Money.of(200), accountId, categoryId, salaryCycleId, null);

        Page<Transaction> page = new PageImpl<>(List.of(income, expense));
        when(transactionRepository.find(any(), any(Pageable.class))).thenReturn(page);

        AccountStatementResult result = service.execute(accountId, today, today.plusDays(1));

        assertEquals(Money.of(1000), result.openingBalance());
        assertEquals(2, result.lines().size());
        assertEquals(Money.of(1500), result.lines().get(0).runningBalance());
        assertEquals(Money.of(1300), result.lines().get(1).runningBalance());
        assertEquals(Money.of(1300), result.endingBalance());
    }

    @Test
    void execute_startsAtZeroWhenNoFromDate() {

        Account account = Account.create("Cash", AccountType.CASH, Money.zero(), null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Page<Transaction> page = new PageImpl<>(List.of());
        when(transactionRepository.find(any(), any(Pageable.class))).thenReturn(page);

        AccountStatementResult result = service.execute(accountId, null, null);

        assertEquals(Money.zero(), result.openingBalance());
    }
}
