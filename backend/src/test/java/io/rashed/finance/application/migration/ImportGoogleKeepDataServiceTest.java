package io.rashed.finance.application.migration;

import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImportGoogleKeepDataServiceTest {

    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    private SalaryCycleRepository salaryCycleRepository;
    private TransactionRepository transactionRepository;
    private ImportGoogleKeepDataService service;

    private Category utilities;
    private Category otherExpense;

    @BeforeEach
    void setUp() {

        accountRepository = mock(AccountRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        salaryCycleRepository = mock(SalaryCycleRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        service = new ImportGoogleKeepDataService(accountRepository, categoryRepository, salaryCycleRepository, transactionRepository);

        utilities = Category.userExpenseCategory("Utilities", null);
        otherExpense = Category.userExpenseCategory("Other Expense", null);

        when(categoryRepository.findByName("Utilities")).thenReturn(Optional.of(utilities));
        when(categoryRepository.findByName("Other Expense")).thenReturn(Optional.of(otherExpense));

        when(transactionRepository.find(any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void execute_createsLegacyImportAccountWhenMissing() {

        when(accountRepository.findByName("Legacy Import")).thenReturn(Optional.empty());
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        givenNoExistingSalaryCycles();

        service.execute("01-26\n=========\n\nGas 1850\n\n=1850\n");

        verify(accountRepository).save(argThat(account -> account.getName().equals("Legacy Import")
                && account.getAccountType() == AccountType.CASH
                && account.isActive()));
    }

    @Test
    void execute_reusesExistingLegacyImportAccountRatherThanCreatingANewOne() {

        Account existing = Account.createCashAccount("Legacy Import", Money.zero());
        when(accountRepository.findByName("Legacy Import")).thenReturn(Optional.of(existing));
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        givenNoExistingSalaryCycles();

        service.execute("01-26\n=========\n\nGas 1850\n\n=1850\n");

        // Only save() call on the account should be the end-of-run deactivation of the SAME account, never a fresh create.
        verify(accountRepository, times(1)).save(argThat(a -> a.getId().equals(existing.getId())));
    }

    @Test
    void execute_deactivatesLegacyImportAccountAfterImport() {

        Account existing = Account.createCashAccount("Legacy Import", Money.zero());
        when(accountRepository.findByName("Legacy Import")).thenReturn(Optional.of(existing));
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        givenNoExistingSalaryCycles();

        service.execute("01-26\n=========\n\nGas 1850\n\n=1850\n");

        verify(accountRepository).save(argThat(account -> account.getId().equals(existing.getId()) && !account.isActive()));
    }

    @Test
    void execute_doesNotRedundantlySaveAnAlreadyDeactivatedAccount() {

        Account alreadyInactive = Account.createCashAccount("Legacy Import", Money.zero()).deactivate();
        when(accountRepository.findByName("Legacy Import")).thenReturn(Optional.of(alreadyInactive));
        givenNoExistingSalaryCycles();

        service.execute("01-26\n=========\n\nGas 1850\n\n=1850\n");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void execute_createsSalaryCycleWhenNoneCoversTheMonth() {

        givenLegacyImportAccountExists();
        when(salaryCycleRepository.findByDate(any())).thenReturn(Optional.empty());
        when(salaryCycleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.execute("01-26\n=========\n\nGas 1850\n\n=1850\n");

        verify(salaryCycleRepository).save(argThat(cycle ->
                cycle.getStartDate().equals(LocalDate.of(2026, 1, 1))
                        && cycle.getEndDate().equals(LocalDate.of(2026, 1, 31))));
    }

    @Test
    void execute_resolvesKnownCategoryAndFallsBackForUnknownLabels() {

        givenLegacyImportAccountExists();
        givenExistingSalaryCycle(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        GoogleKeepMigrationResult result = service.execute(
                "01-26\n=========\n\nগ্যাস 1850\n\nSomeUnknownLabel 500\n\n=2350\n");

        assertEquals(2, result.importedCount());

        verify(transactionRepository).save(argThat(t -> t.getCategoryId().equals(utilities.getId())));
        verify(transactionRepository).save(argThat(t -> t.getCategoryId().equals(otherExpense.getId())));
    }

    @Test
    void execute_tagsCreatedTransactionsWithMigrationBatchId() {

        givenLegacyImportAccountExists();
        givenExistingSalaryCycle(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        service.execute("01-26\n=========\n\nGas 1850\n\n=1850\n");

        verify(transactionRepository).save(argThat(t ->
                t.getMigrationBatchId() != null && t.getMigrationBatchId().startsWith("google-keep-")));
    }

    @Test
    void execute_skipsExactDuplicates() {

        givenLegacyImportAccountExists();
        SalaryCycle cycle = givenExistingSalaryCycle(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        when(categoryRepository.findByName("Other Expense")).thenReturn(Optional.of(otherExpense));

        Account account = Account.createCashAccount("Legacy Import", Money.zero());
        Transaction existing = Transaction.expense(
                TransactionId.newId(), cycle.getStartDate(), Money.of(500), account.getId(), otherExpense.getId(), cycle.getId(), "Gas");

        Page<Transaction> existingPage = new PageImpl<>(List.of(existing));
        when(transactionRepository.find(any(), any())).thenReturn(existingPage);

        GoogleKeepMigrationResult result = service.execute("01-26\n=========\n\nGas 500\n\n=500\n");

        assertEquals(0, result.importedCount());
        assertEquals(1, result.skippedCount());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void execute_warnsOnStatedTotalMismatchButDoesNotBlock() {

        givenLegacyImportAccountExists();
        givenExistingSalaryCycle(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        GoogleKeepMigrationResult result = service.execute("01-26\n=========\n\nGas 1850\n\n=9999\n");

        assertEquals(1, result.importedCount());
        assertTrue(result.warnings().stream().anyMatch(w -> w.contains("does not match")));
    }

    private void givenLegacyImportAccountExists() {

        Account account = Account.createCashAccount("Legacy Import", Money.zero());
        when(accountRepository.findByName("Legacy Import")).thenReturn(Optional.of(account));
    }

    private void givenNoExistingSalaryCycles() {

        when(salaryCycleRepository.findByDate(any())).thenReturn(Optional.empty());
        when(salaryCycleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private SalaryCycle givenExistingSalaryCycle(LocalDate start, LocalDate end) {

        SalaryCycle cycle = SalaryCycle.create("January 2026", start, end, start, null);
        when(salaryCycleRepository.findByDate(eq(start))).thenReturn(Optional.of(cycle));
        return cycle;
    }
}
