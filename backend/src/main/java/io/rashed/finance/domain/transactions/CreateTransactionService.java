package io.rashed.finance.domain.transactions;

import java.util.Objects;

public class CreateTransactionService {

    private final TransactionRepository transactionRepository;

    public CreateTransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
    }

    public Transaction execute(CreateTransactionCommand command) {

        Objects.requireNonNull(command, "Command cannot be null");

        Transaction transaction = switch (command.transactionType()) {

            case EXPENSE -> Transaction.expense(
                    TransactionId.newId(),
                    command.transactionDate(),
                    command.amount(),
                    command.fromAccountId(),
                    command.categoryId(),
                    command.salaryCycleId(),
                    command.description()
            );

            case INCOME -> Transaction.income(
                    TransactionId.newId(),
                    command.transactionDate(),
                    command.amount(),
                    command.toAccountId(),
                    command.categoryId(),
                    command.salaryCycleId(),
                    command.description()
            );

            case TRANSFER -> Transaction.transfer(
                    TransactionId.newId(),
                    command.transactionDate(),
                    command.amount(),
                    command.fromAccountId(),
                    command.toAccountId(),
                    command.salaryCycleId(),
                    command.description()
            );

            case ADJUSTMENT -> Transaction.adjustment(
                    TransactionId.newId(),
                    command.transactionDate(),
                    command.amount(),
                    command.fromAccountId(),
                    command.adjustmentReason(),
                    command.description()
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
}