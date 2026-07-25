package io.rashed.finance.application.recurring;

import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.enums.Frequency;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateRecurringTransactionServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final CategoryId categoryId = CategoryId.newId();
    private final LocalDate startDate = LocalDate.of(2026, 1, 1);

    private RecurringTransactionRepository repository;
    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    private CreateRecurringTransactionService service;

    @BeforeEach
    void setUp() {

        repository = mock(RecurringTransactionRepository.class);
        accountRepository = mock(AccountRepository.class);
        categoryRepository = mock(CategoryRepository.class);

        service = new CreateRecurringTransactionService(repository, accountRepository, categoryRepository);

        when(repository.save(any(RecurringTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void create_expenseSucceedsWithActiveAccountAndCategory() {

        givenActiveAccount();
        givenExpenseCategory();

        CreateRecurringTransactionCommand command = new CreateRecurringTransactionCommand(
                "House Rent", TransactionType.EXPENSE, accountId, null, categoryId,
                Money.of(10000), Frequency.MONTHLY, startDate, null, false, null, null);

        RecurringTransaction template = service.create(command);

        assertEquals(TransactionType.EXPENSE, template.getTransactionType());
    }

    @Test
    void create_rejectsUnknownAccount() {

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        givenExpenseCategory();

        CreateRecurringTransactionCommand command = new CreateRecurringTransactionCommand(
                "House Rent", TransactionType.EXPENSE, accountId, null, categoryId,
                Money.of(10000), Frequency.MONTHLY, startDate, null, false, null, null);

        assertThrows(ResourceNotFoundException.class, () -> service.create(command));
    }

    @Test
    void create_rejectsInactiveAccount() {

        Account account = Account.create("Cash", AccountType.CASH, Money.zero(), null).deactivate();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        givenExpenseCategory();

        CreateRecurringTransactionCommand command = new CreateRecurringTransactionCommand(
                "House Rent", TransactionType.EXPENSE, accountId, null, categoryId,
                Money.of(10000), Frequency.MONTHLY, startDate, null, false, null, null);

        assertThrows(TransactionValidationException.class, () -> service.create(command));
    }

    @Test
    void create_rejectsIncomeCategoryOnExpense() {

        givenActiveAccount();
        Category income = Category.userIncomeCategory("Salary", null);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(income));

        CreateRecurringTransactionCommand command = new CreateRecurringTransactionCommand(
                "House Rent", TransactionType.EXPENSE, accountId, null, categoryId,
                Money.of(10000), Frequency.MONTHLY, startDate, null, false, null, null);

        assertThrows(TransactionValidationException.class, () -> service.create(command));
    }

    @Test
    void create_transferSucceedsWithoutCategory() {

        AccountId toAccountId = AccountId.newId();
        givenActiveAccount();
        Account otherAccount = Account.create("Savings", AccountType.SAVINGS, Money.zero(), null);
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(otherAccount));

        CreateRecurringTransactionCommand command = new CreateRecurringTransactionCommand(
                "Savings Sweep", TransactionType.TRANSFER, accountId, toAccountId, null,
                Money.of(500), Frequency.MONTHLY, startDate, null, true, null, null);

        RecurringTransaction template = service.create(command);

        assertEquals(TransactionType.TRANSFER, template.getTransactionType());
    }

    private void givenActiveAccount() {

        Account account = Account.create("Cash", AccountType.CASH, Money.zero(), null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    }

    private void givenExpenseCategory() {

        Category category = Category.userExpenseCategory("Rent", null);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
    }
}
