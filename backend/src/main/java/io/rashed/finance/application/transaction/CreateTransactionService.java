package io.rashed.finance.application.transaction;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.application.salarycycle.OpenSalaryCycleForIncomeService;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.funds.FundRepository;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;

@Service
public class CreateTransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final SalaryCycleRepository salaryCycleRepository;
    private final FundRepository fundRepository;
    private final OpenSalaryCycleForIncomeService openSalaryCycleForIncomeService;

    public CreateTransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            SalaryCycleRepository salaryCycleRepository,
            FundRepository fundRepository,
            OpenSalaryCycleForIncomeService openSalaryCycleForIncomeService
    ) {
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.categoryRepository = Objects.requireNonNull(categoryRepository);
        this.salaryCycleRepository = Objects.requireNonNull(salaryCycleRepository);
        this.fundRepository = Objects.requireNonNull(fundRepository);
        this.openSalaryCycleForIncomeService = Objects.requireNonNull(openSalaryCycleForIncomeService);
    }

    public Transaction execute(CreateTransactionCommand command) {

        Objects.requireNonNull(command, "Command cannot be null");

        validateAccount(command.fromAccountId(), "Source");
        validateAccount(command.toAccountId(), "Destination");
        validateCategory(command.categoryId(), command.transactionType());
        validateFund(command.fundId());

        SalaryCycleId salaryCycleId;

        if (command.transactionType() == TransactionType.INCOME && command.startsNewSalaryCycle()) {
            salaryCycleId = openSalaryCycleForIncomeService.execute(command.transactionDate());
        } else {
            validateSalaryCycle(command.salaryCycleId());
            salaryCycleId = command.salaryCycleId();
        }

        if (command.transactionType() == TransactionType.OPENING_BALANCE) {
            validateSingleOpeningBalance(command.toAccountId());
        }

        Transaction transaction = switch (command.transactionType()) {

            case EXPENSE -> Transaction.expense(
                    TransactionId.newId(),
                    command.transactionDate(),
                    command.amount(),
                    command.fromAccountId(),
                    command.categoryId(),
                    salaryCycleId,
                    command.description()
            );

            case INCOME -> Transaction.income(
                    TransactionId.newId(),
                    command.transactionDate(),
                    command.amount(),
                    command.toAccountId(),
                    command.categoryId(),
                    salaryCycleId,
                    command.description()
            );

            case TRANSFER -> command.fundId() != null
                    ? Transaction.fundTransfer(
                            TransactionId.newId(),
                            command.transactionDate(),
                            command.amount(),
                            command.fromAccountId(),
                            command.toAccountId(),
                            command.fundId(),
                            salaryCycleId,
                            command.description()
                    )
                    : Transaction.transfer(
                            TransactionId.newId(),
                            command.transactionDate(),
                            command.amount(),
                            command.fromAccountId(),
                            command.toAccountId(),
                            salaryCycleId,
                            command.description()
                    );

            case ADJUSTMENT -> Transaction.adjustment(
                    TransactionId.newId(),
                    command.transactionDate(),
                    command.amount(),
                    command.fromAccountId(),
                    command.toAccountId(),
                    command.referenceTransactionId(),
                    command.adjustmentReason(),
                    command.description(),
                    command.notes()
            );

            case OPENING_BALANCE -> Transaction.openingBalance(
                    TransactionId.newId(),
                    command.transactionDate(),
                    command.amount(),
                    command.toAccountId()
            );

            case MIGRATION -> Transaction.migration(
                    TransactionId.newId(),
                    command.transactionDate(),
                    command.amount(),
                    command.toAccountId(),
                    command.migrationBatchId(),
                    command.description()
            );
        };

        return transactionRepository.save(transaction);
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

    private void validateFund(FundId fundId) {

        if (fundId == null) {
            return;
        }

        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new ResourceNotFoundException("Fund not found."));

        if (fund.isInactive()) {
            throw new TransactionValidationException("Fund is not active.");
        }
    }

    private void validateSalaryCycle(SalaryCycleId salaryCycleId) {

        if (salaryCycleId == null) {
            return;
        }

        if (salaryCycleRepository.findById(salaryCycleId).isEmpty()) {
            throw new ResourceNotFoundException("Salary cycle not found.");
        }
    }

    private void validateSingleOpeningBalance(AccountId accountId) {

        if (accountId != null && transactionRepository.existsOpeningBalanceForAccount(accountId)) {
            throw new TransactionValidationException("Account already has an opening balance transaction.");
        }
    }
}