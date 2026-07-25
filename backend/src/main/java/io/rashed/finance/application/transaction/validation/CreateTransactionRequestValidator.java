package io.rashed.finance.application.transaction.validation;

import io.rashed.finance.api.dto.transaction.CreateTransactionRequest;
import io.rashed.finance.common.exception.TransactionValidationException;

public final class CreateTransactionRequestValidator {

    private CreateTransactionRequestValidator() {
    }

    public static void validate(CreateTransactionRequest request) {

        switch (request.transactionType()) {

            case EXPENSE -> validateExpense(request);

            case INCOME -> validateIncome(request);

            case TRANSFER -> validateTransfer(request);

            case ADJUSTMENT -> validateAdjustment(request);

            case OPENING_BALANCE -> validateOpeningBalance(request);

            case MIGRATION -> validateMigration(request);

            default -> throw new TransactionValidationException(
                    "Unsupported transaction type: " + request.transactionType());
        }
    }

    private static void validateExpense(CreateTransactionRequest request) {

        require(request.fromAccountId() != null,
                "Expense requires fromAccountId.");

        require(request.categoryId() != null,
                "Expense requires categoryId.");
    }

    private static void validateIncome(CreateTransactionRequest request) {

        require(request.toAccountId() != null,
                "Income requires toAccountId.");

        require(request.categoryId() != null,
                "Income requires categoryId.");
    }

    private static void validateTransfer(CreateTransactionRequest request) {

        require(request.fromAccountId() != null,
                "Transfer requires fromAccountId.");

        require(request.toAccountId() != null,
                "Transfer requires toAccountId.");

        require(!request.fromAccountId().equals(request.toAccountId()),
                "Source and destination account cannot be same.");
    }

    private static void validateAdjustment(CreateTransactionRequest request) {

        require((request.fromAccountId() == null) != (request.toAccountId() == null),
                "Adjustment requires exactly one of fromAccountId or toAccountId.");

        require(request.adjustmentReason() != null,
                "Adjustment requires adjustmentReason.");
    }

    private static void validateOpeningBalance(CreateTransactionRequest request) {

        require(request.toAccountId() != null,
                "Opening balance requires account.");
    }

    private static void validateMigration(CreateTransactionRequest request) {

        require(request.toAccountId() != null,
                "Migration requires account.");

        require(request.migrationBatchId() != null && !request.migrationBatchId().isBlank(),
                "Migration requires migrationBatchId.");
    }

    private static void require(boolean condition, String message) {

        if (!condition) {
            throw new TransactionValidationException(message);
        }
    }
}