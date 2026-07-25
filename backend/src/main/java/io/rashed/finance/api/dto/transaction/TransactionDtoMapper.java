package io.rashed.finance.api.dto.transaction;

import io.rashed.finance.application.transaction.CreateTransactionCommand;
import io.rashed.finance.application.transaction.UpdateTransactionCommand;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import java.util.UUID;

public final class TransactionDtoMapper {

    private TransactionDtoMapper() {
    }

    public static CreateTransactionCommand toCommand(CreateTransactionRequest request) {

        return new CreateTransactionCommand(
                request.transactionType(),
                request.transactionDate(),
                Money.of(request.amount()),
                request.description(),
                request.notes(),
                request.fromAccountId() == null ? null : AccountId.of(request.fromAccountId()),
                request.toAccountId() == null ? null : AccountId.of(request.toAccountId()),
                request.categoryId() == null ? null : CategoryId.of(request.categoryId()),
                request.salaryCycleId() == null ? null : SalaryCycleId.of(request.salaryCycleId()),
                null,
                request.migrationBatchId(),
                null,
                request.adjustmentReason(),
                request.referenceTransactionId() == null ? null : TransactionId.of(request.referenceTransactionId()),
                request.fundId() == null ? null : FundId.of(request.fundId()),
                request.startsNewSalaryCycle()
        );
    }

    public static TransactionResponse toResponse(Transaction transaction) {

        return new TransactionResponse(
                transaction.getId().getValue(),
                transaction.getTransactionType(),
                transaction.getTransactionStatus(),
                transaction.getTransactionDate(),
                transaction.getAmount().getAmount(),
                transaction.getFromAccountId() != null ? transaction.getFromAccountId().getValue() : null,
                transaction.getToAccountId() != null ? transaction.getToAccountId().getValue() : null,
                transaction.getCategoryId() != null ? transaction.getCategoryId().getValue() : null,
                transaction.getSalaryCycleId() != null ? transaction.getSalaryCycleId().getValue() : null,
                transaction.getReferenceNumber(),
                transaction.getFundId() != null ? transaction.getFundId().getValue() : null,
                transaction.getLoanId() != null ? transaction.getLoanId().getValue() : null,
                transaction.getAdjustmentReason(),
                transaction.getDescription(),
                transaction.getNotes(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    public static UpdateTransactionCommand toCommand(
                UUID id,
                UpdateTransactionRequest request
        ) {


                return new UpdateTransactionCommand(

                        TransactionId.of(id),

                        Money.of(
                                request.amount()
                        ),

                        request.categoryId() == null
                                ? null
                                : CategoryId.of(
                                        request.categoryId()
                                ),

                        request.description(),

                        request.notes()

                );

        }
}