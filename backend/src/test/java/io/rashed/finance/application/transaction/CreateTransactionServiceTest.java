package io.rashed.finance.application.transaction;

import io.rashed.finance.application.salarycycle.OpenSalaryCycleForIncomeService;
import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.common.enums.FundType;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.funds.FundRepository;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateTransactionServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final CategoryId categoryId = CategoryId.newId();
    private final SalaryCycleId salaryCycleId = SalaryCycleId.newId();
    private final FundId fundId = FundId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    private SalaryCycleRepository salaryCycleRepository;
    private FundRepository fundRepository;
    private OpenSalaryCycleForIncomeService openSalaryCycleForIncomeService;
    private CreateTransactionService service;

    @BeforeEach
    void setUp() {

        transactionRepository = mock(TransactionRepository.class);
        accountRepository = mock(AccountRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        salaryCycleRepository = mock(SalaryCycleRepository.class);
        fundRepository = mock(FundRepository.class);
        openSalaryCycleForIncomeService = mock(OpenSalaryCycleForIncomeService.class);

        service = new CreateTransactionService(
                transactionRepository, accountRepository, categoryRepository, salaryCycleRepository,
                fundRepository, openSalaryCycleForIncomeService);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void execute_createsExpenseWhenAccountAndCategoryAreValid() {

        givenActiveAccount(accountId);
        givenExpenseCategory(categoryId, true);
        givenSalaryCycleExists();

        Transaction transaction = service.execute(expenseCommand());

        assertEquals(TransactionType.EXPENSE, transaction.getTransactionType());
    }

    @Test
    void execute_rejectsUnknownAccount() {

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        givenExpenseCategory(categoryId, true);
        givenSalaryCycleExists();

        assertThrows(ResourceNotFoundException.class, () -> service.execute(expenseCommand()));
    }

    @Test
    void execute_rejectsInactiveAccount() {

        givenActiveAccount(accountId, false);
        givenExpenseCategory(categoryId, true);
        givenSalaryCycleExists();

        assertThrows(TransactionValidationException.class, () -> service.execute(expenseCommand()));
    }

    @Test
    void execute_rejectsUnknownCategory() {

        givenActiveAccount(accountId);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        givenSalaryCycleExists();

        assertThrows(ResourceNotFoundException.class, () -> service.execute(expenseCommand()));
    }

    @Test
    void execute_rejectsInactiveCategory() {

        givenActiveAccount(accountId);
        givenExpenseCategory(categoryId, false);
        givenSalaryCycleExists();

        assertThrows(TransactionValidationException.class, () -> service.execute(expenseCommand()));
    }

    @Test
    void execute_rejectsIncomeCategoryOnExpense() {

        givenActiveAccount(accountId);
        givenIncomeCategory(categoryId, true);
        givenSalaryCycleExists();

        assertThrows(TransactionValidationException.class, () -> service.execute(expenseCommand()));
    }

    @Test
    void execute_rejectsUnknownSalaryCycle() {

        givenActiveAccount(accountId);
        givenExpenseCategory(categoryId, true);
        when(salaryCycleRepository.findById(salaryCycleId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(expenseCommand()));
    }

    @Test
    void execute_resolvesSalaryCycleFromAutomationWhenStartsNewSalaryCycleIsSet() {

        givenActiveAccount(accountId);
        Category income = Category.userIncomeCategory("Salary", null);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(income));

        SalaryCycleId resolvedId = SalaryCycleId.newId();
        when(openSalaryCycleForIncomeService.execute(today)).thenReturn(resolvedId);

        CreateTransactionCommand command = new CreateTransactionCommand(
                TransactionType.INCOME, today, Money.of(80000), "Salary", null,
                null, accountId, categoryId, null, null, null, null, null, null, null, true);

        Transaction transaction = service.execute(command);

        assertEquals(resolvedId, transaction.getSalaryCycleId());
    }

    @Test
    void execute_rejectsSecondOpeningBalanceForSameAccount() {

        givenActiveAccount(accountId);
        when(transactionRepository.existsOpeningBalanceForAccount(accountId)).thenReturn(true);

        CreateTransactionCommand command = new CreateTransactionCommand(
                TransactionType.OPENING_BALANCE, today, Money.of(1000), "Opening", null,
                null, accountId, null, null, null, null, null, null, null, null, false);

        assertThrows(TransactionValidationException.class, () -> service.execute(command));
    }

    @Test
    void execute_allowsFirstOpeningBalanceForAccount() {

        givenActiveAccount(accountId);
        when(transactionRepository.existsOpeningBalanceForAccount(accountId)).thenReturn(false);

        CreateTransactionCommand command = new CreateTransactionCommand(
                TransactionType.OPENING_BALANCE, today, Money.of(1000), "Opening", null,
                null, accountId, null, null, null, null, null, null, null, null, false);

        Transaction transaction = service.execute(command);

        assertEquals(TransactionType.OPENING_BALANCE, transaction.getTransactionType());
    }

    @Test
    void execute_createsFundAllocationWhenFundIsActive() {

        givenActiveAccount(accountId);
        givenSalaryCycleExists();
        givenActiveFund(fundId, true);

        CreateTransactionCommand command = new CreateTransactionCommand(
                TransactionType.TRANSFER, today, Money.of(500), "Save", null,
                accountId, null, null, salaryCycleId, null, null, null, null, null, fundId, false);

        Transaction transaction = service.execute(command);

        assertEquals(fundId, transaction.getFundId());
        assertEquals(accountId, transaction.getFromAccountId());
    }

    @Test
    void execute_rejectsUnknownFund() {

        givenActiveAccount(accountId);
        givenSalaryCycleExists();
        when(fundRepository.findById(fundId)).thenReturn(Optional.empty());

        CreateTransactionCommand command = new CreateTransactionCommand(
                TransactionType.TRANSFER, today, Money.of(500), "Save", null,
                accountId, null, null, salaryCycleId, null, null, null, null, null, fundId, false);

        assertThrows(ResourceNotFoundException.class, () -> service.execute(command));
    }

    @Test
    void execute_rejectsInactiveFund() {

        givenActiveAccount(accountId);
        givenSalaryCycleExists();
        givenActiveFund(fundId, false);

        CreateTransactionCommand command = new CreateTransactionCommand(
                TransactionType.TRANSFER, today, Money.of(500), "Save", null,
                accountId, null, null, salaryCycleId, null, null, null, null, null, fundId, false);

        assertThrows(TransactionValidationException.class, () -> service.execute(command));
    }

    private void givenActiveFund(FundId id, boolean active) {

        Fund fund = Fund.create("Vacation", FundType.SAVINGS, null, null, null);

        if (!active) {
            fund = fund.deactivate();
        }

        when(fundRepository.findById(id)).thenReturn(Optional.of(fund));
    }

    private CreateTransactionCommand expenseCommand() {

        return new CreateTransactionCommand(
                TransactionType.EXPENSE, today, Money.of(500), "Groceries", null,
                accountId, null, categoryId, salaryCycleId, null, null, null, null, null, null, false);
    }

    private void givenActiveAccount(AccountId id) {
        givenActiveAccount(id, true);
    }

    private void givenActiveAccount(AccountId id, boolean active) {

        Account account = Account.create("Cash", AccountType.CASH, Money.zero(), null);

        if (!active) {
            account = account.deactivate();
        }

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
    }

    private void givenExpenseCategory(CategoryId id, boolean active) {

        Category category = Category.userExpenseCategory("Groceries", null);

        if (!active) {
            category = category.deactivate();
        }

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
    }

    private void givenIncomeCategory(CategoryId id, boolean active) {

        Category category = Category.userIncomeCategory("Salary", null);

        if (!active) {
            category = category.deactivate();
        }

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
    }

    private void givenSalaryCycleExists() {

        SalaryCycle salaryCycle = SalaryCycle.create(
                "July 2026",
                today.withDayOfMonth(1),
                today.withDayOfMonth(today.lengthOfMonth()),
                today,
                null
        );

        when(salaryCycleRepository.findById(salaryCycleId)).thenReturn(Optional.of(salaryCycle));
    }
}
