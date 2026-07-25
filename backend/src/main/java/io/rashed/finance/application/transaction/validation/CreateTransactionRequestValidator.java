package io.rashed.finance.application.transaction.validation;

import io.rashed.finance.api.dto.transaction.CreateTransactionRequest;
import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.exception.TransactionValidationException;

public final class CreateTransactionRequestValidator {

    private CreateTransactionRequestValidator() {
    }

    public static void validate(CreateTransactionRequest request) {

        require(!request.startsNewSalaryCycle() || request.transactionType() == TransactionType.INCOME,
                "Only an income transaction can start a new salary cycle.");

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

        require(request.salaryCycleId() != null,
                "Expense requires salaryCycleId.");
    }

    private static void validateIncome(CreateTransactionRequest request) {

        require(request.toAccountId() != null,
                "Income requires toAccountId.");

        require(request.categoryId() != null,
                "Income requires categoryId.");

        require(request.startsNewSalaryCycle() || request.salaryCycleId() != null,
                "Income requires salaryCycleId unless it starts a new salary cycle.");
    }

    private static void validateTransfer(CreateTransactionRequest request) {

        require(request.categoryId() == null,
                "Transfer transactions cannot have a category.");

        require(request.salaryCycleId() != null,
                "Transfer requires salaryCycleId.");

        if (request.fundId() != null) {

            require((request.fromAccountId() == null) != (request.toAccountId() == null),
                    "A fund transfer requires exactly one of fromAccountId or toAccountId.");

            return;
        }

        require(request.fromAccountId() != null,
                "Transfer requires fromAccountId.");

        require(request.toAccountId() != null,
                "Transfer requires toAccountId.");

        require(!request.fromAccountId().equals(request.toAccountId()),
                "Source and destination account cannot be same.");
    }

    private static void validateAdjustment(CreateTransactionRequest request) {

        require(request.fromAccountId() == null || request.toAccountId() == null,
                "Adjustment cannot reference both fromAccountId and toAccountId.");

        require(request.adjustmentReason() != null,
                "Adjustment requires adjustmentReason.");

        require(request.adjustmentReason() != AdjustmentReason.MANUAL_CORRECTION
                        || (request.notes() != null && !request.notes().isBlank()),
                "Manual correction requires notes explaining the adjustment.");
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