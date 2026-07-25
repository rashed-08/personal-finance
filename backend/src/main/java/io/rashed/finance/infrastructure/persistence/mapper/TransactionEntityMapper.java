package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.infrastructure.persistence.entity.TransactionEntity;

public final class TransactionEntityMapper {

    private TransactionEntityMapper() {
    }

    public static TransactionEntity toEntity(Transaction transaction) {

        if (transaction == null) {
            return null;
        }

        return new TransactionEntity(
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
                transaction.getMigrationBatchId(),
                transaction.getReconciliationBatchId(),
                transaction.getAdjustmentReason(),
                transaction.getDescription(),
                transaction.getNotes(),
                transaction.getReferenceTransactionId() != null ? transaction.getReferenceTransactionId().getValue() : null,
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    public static Transaction toDomain(TransactionEntity entity) {

        if (entity == null) {
            return null;
        }

        return new Transaction(
                TransactionId.of(entity.getId()),
                entity.getTransactionType(),
                entity.getTransactionStatus(),
                entity.getTransactionDate(),
                Money.of(entity.getAmount()),
                entity.getDescription(),
                entity.getNotes(),
                entity.getFromAccountId() != null ? AccountId.of(entity.getFromAccountId()) : null,
                entity.getToAccountId() != null ? AccountId.of(entity.getToAccountId()) : null,
                entity.getCategoryId() != null ? CategoryId.of(entity.getCategoryId()) : null,
                entity.getSalaryCycleId() != null ? SalaryCycleId.of(entity.getSalaryCycleId()) : null,
                entity.getReferenceNumber(),
                entity.getMigrationBatchId(),
                entity.getReconciliationBatchId(),
                entity.getAdjustmentReason(),
                entity.getReferenceTransactionId() != null ? TransactionId.of(entity.getReferenceTransactionId()) : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}