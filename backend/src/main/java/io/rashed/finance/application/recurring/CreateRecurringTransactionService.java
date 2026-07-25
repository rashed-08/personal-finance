package io.rashed.finance.application.recurring;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;

@Service
public class CreateRecurringTransactionService {

    private final RecurringTransactionRepository repository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public CreateRecurringTransactionService(
            RecurringTransactionRepository repository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.categoryRepository = Objects.requireNonNull(categoryRepository);
    }

    public RecurringTransaction create(CreateRecurringTransactionCommand command) {

        Objects.requireNonNull(command);

        validateAccount(command.fromAccountId(), "Source");
        validateAccount(command.toAccountId(), "Destination");
        validateCategory(command.categoryId(), command.transactionType());

        RecurringTransaction recurringTransaction = switch (command.transactionType()) {

            case EXPENSE -> RecurringTransaction.expense(
                    command.name(),
                    command.fromAccountId(),
                    command.categoryId(),
                    command.amount(),
                    command.frequency(),
                    command.startDate(),
                    command.endDate(),
                    command.autoGenerate(),
                    command.description(),
                    command.notes()
            );

            case INCOME -> RecurringTransaction.income(
                    command.name(),
                    command.toAccountId(),
                    command.categoryId(),
                    command.amount(),
                    command.frequency(),
                    command.startDate(),
                    command.endDate(),
                    command.autoGenerate(),
                    command.description(),
                    command.notes()
            );

            case TRANSFER -> RecurringTransaction.transfer(
                    command.name(),
                    command.fromAccountId(),
                    command.toAccountId(),
                    command.amount(),
                    command.frequency(),
                    command.startDate(),
                    command.endDate(),
                    command.autoGenerate(),
                    command.description(),
                    command.notes()
            );

            default -> throw new TransactionValidationException(
                    "Recurring transactions support only EXPENSE, INCOME and TRANSFER.");
        };

        return repository.save(recurringTransaction);
    }

    private void validateAccount(AccountId accountId, String role) {

        if (accountId == null) {
            return;
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(role + " account not found."));

        if (account.isInactive()) {
            throw new TransactionValidationException(role + " account is not active.");
        }
    }

    private void validateCategory(CategoryId categoryId, TransactionType transactionType) {

        if (categoryId == null) {
            return;
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));

        if (!category.isActive()) {
            throw new TransactionValidationException("Category is not active.");
        }

        if (transactionType == TransactionType.INCOME && !category.isIncome()) {
            throw new TransactionValidationException("Income transactions require an income category.");
        }

        if (transactionType == TransactionType.EXPENSE && !category.isExpense()) {
            throw new TransactionValidationException("Expense transactions require an expense category.");
        }
    }
}
