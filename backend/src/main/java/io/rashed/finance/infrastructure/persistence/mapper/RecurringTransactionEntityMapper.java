package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionId;
import io.rashed.finance.infrastructure.persistence.entity.RecurringTransactionEntity;

public final class RecurringTransactionEntityMapper {

    private RecurringTransactionEntityMapper() {
    }

    public static RecurringTransactionEntity toEntity(RecurringTransaction recurringTransaction) {

        if (recurringTransaction == null) {
            return null;
        }

        return new RecurringTransactionEntity(
                recurringTransaction.getId().getValue(),
                recurringTransaction.getName(),
                recurringTransaction.getTransactionType(),
                recurringTransaction.getAmount().getAmount(),
                recurringTransaction.getDescription(),
                recurringTransaction.getNotes(),
                recurringTransaction.getFromAccountId() != null ? recurringTransaction.getFromAccountId().getValue() : null,
                recurringTransaction.getToAccountId() != null ? recurringTransaction.getToAccountId().getValue() : null,
                recurringTransaction.getCategoryId() != null ? recurringTransaction.getCategoryId().getValue() : null,
                recurringTransaction.getFrequency(),
                recurringTransaction.getStartDate(),
                recurringTransaction.getEndDate(),
                recurringTransaction.getNextExecutionDate(),
                recurringTransaction.getLastExecutionDate(),
                recurringTransaction.isAutoGenerate(),
                recurringTransaction.isActive(),
                recurringTransaction.getCreatedAt(),
                recurringTransaction.getUpdatedAt()
        );
    }

    public static RecurringTransaction toDomain(RecurringTransactionEntity entity) {

        if (entity == null) {
            return null;
        }

        return new RecurringTransaction(
                RecurringTransactionId.of(entity.getId()),
                entity.getName(),
                entity.getTransactionType(),
                entity.getFrequency(),
                Money.of(entity.getAmount()),
                entity.getFromAccountId() != null ? AccountId.of(entity.getFromAccountId()) : null,
                entity.getToAccountId() != null ? AccountId.of(entity.getToAccountId()) : null,
                entity.getCategoryId() != null ? CategoryId.of(entity.getCategoryId()) : null,
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getNextExecutionDate(),
                entity.getLastExecutionDate(),
                entity.isAutoGenerate(),
                entity.isActive(),
                entity.getDescription(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
