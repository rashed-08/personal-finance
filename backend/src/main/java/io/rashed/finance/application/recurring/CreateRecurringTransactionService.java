package io.rashed.finance.application.recurring;

import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionRepository;

public class CreateRecurringTransactionService {

    private final RecurringTransactionRepository repository;

    public CreateRecurringTransactionService(
            RecurringTransactionRepository repository
    ) {
        this.repository = repository;
    }

    public RecurringTransaction create(
            CreateRecurringTransactionCommand command
    ) {

        RecurringTransaction recurringTransaction;

        if (command.transactionType() == TransactionType.EXPENSE) {

            recurringTransaction =
                    RecurringTransaction.expense(
                            command.fromAccountId(),
                            command.categoryId(),
                            command.amount(),
                            command.frequency(),
                            command.startDate(),
                            command.endDate(),
                            command.description()
                    );

        } else if (command.transactionType() == TransactionType.INCOME) {

            recurringTransaction =
                    RecurringTransaction.income(
                            command.toAccountId(),
                            command.categoryId(),
                            command.amount(),
                            command.frequency(),
                            command.startDate(),
                            command.endDate(),
                            command.description()
                    );

        } else {

            throw new IllegalArgumentException(
                    "Recurring transaction supports only EXPENSE and INCOME."
            );
        }

        return repository.save(recurringTransaction);
    }
}