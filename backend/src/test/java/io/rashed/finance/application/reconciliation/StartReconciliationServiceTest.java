package io.rashed.finance.application.reconciliation;

import io.rashed.finance.application.account.CalculateAccountBalanceService;
import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StartReconciliationServiceTest {

    private final LocalDate today = LocalDate.of(2026, 7, 25);
    private final AccountId accountId = AccountId.newId();

    private CashReconciliationRepository repository;
    private AccountRepository accountRepository;
    private CalculateAccountBalanceService calculateAccountBalanceService;
    private StartReconciliationService service;

    @BeforeEach
    void setUp() {

        repository = mock(CashReconciliationRepository.class);
        accountRepository = mock(AccountRepository.class);
        calculateAccountBalanceService = mock(CalculateAccountBalanceService.class);

        service = new StartReconciliationService(repository, accountRepository, calculateAccountBalanceService);

        when(repository.save(any(CashReconciliation.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void execute_startsAReconciliationWithComputedExpectedCash() {

        Account cashAccount = Account.createCashAccount("Wallet", Money.zero());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(cashAccount));
        when(repository.existsPendingForAccount(accountId)).thenReturn(false);
        when(calculateAccountBalanceService.execute(accountId, today)).thenReturn(Money.of(2200));

        CashReconciliation reconciliation = service.execute(
                new StartReconciliationCommand(accountId, today, null));

        assertEquals(Money.of(2200), reconciliation.getExpectedCashAmount());
        assertEquals(accountId, reconciliation.getAccountId());
    }

    @Test
    void execute_rejectsUnknownAccount() {

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.execute(new StartReconciliationCommand(accountId, today, null)));
    }

    @Test
    void execute_rejectsInactiveAccount() {

        Account inactive = Account.createCashAccount("Wallet", Money.zero()).deactivate();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(inactive));

        assertThrows(TransactionValidationException.class, () ->
                service.execute(new StartReconciliationCommand(accountId, today, null)));
    }

    @Test
    void execute_rejectsNonCashAccount() {

        Account bank = Account.createBankAccount("Bank", Money.zero());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(bank));

        assertThrows(TransactionValidationException.class, () ->
                service.execute(new StartReconciliationCommand(accountId, today, null)));
    }

    @Test
    void execute_rejectsWhenAnotherReconciliationIsAlreadyPendingForTheAccount() {

        Account cashAccount = Account.createCashAccount("Wallet", Money.zero());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(cashAccount));
        when(repository.existsPendingForAccount(accountId)).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                service.execute(new StartReconciliationCommand(accountId, today, null)));
    }
}
