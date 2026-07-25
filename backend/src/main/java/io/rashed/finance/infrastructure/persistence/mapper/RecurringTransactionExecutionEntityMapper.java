package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.domain.recurring.RecurringTransactionExecution;
import io.rashed.finance.domain.recurring.RecurringTransactionExecutionId;
import io.rashed.finance.domain.recurring.RecurringTransactionId;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.infrastructure.persistence.entity.RecurringTransactionExecutionEntity;

public final class RecurringTransactionExecutionEntityMapper {

    private RecurringTransactionExecutionEntityMapper() {
    }

    public static RecurringTransactionExecutionEntity toEntity(RecurringTransactionExecution execution) {

        if (execution == null) {
            return null;
        }

        return new RecurringTransactionExecutionEntity(
                execution.getId().getValue(),
                execution.getRecurringTransactionId().getValue(),
                execution.getScheduledDate(),
                execution.getStatus(),
                execution.getTransactionId() != null ? execution.getTransactionId().getValue() : null,
                execution.getReason(),
                execution.getCreatedAt()
        );
    }

    public static RecurringTransactionExecution toDomain(RecurringTransactionExecutionEntity entity) {

        if (entity == null) {
            return null;
        }

        return new RecurringTransactionExecution(
                RecurringTransactionExecutionId.of(entity.getId()),
                RecurringTransactionId.of(entity.getRecurringTransactionId()),
                entity.getScheduledDate(),
                entity.getStatus(),
                entity.getTransactionId() != null ? TransactionId.of(entity.getTransactionId()) : null,
                entity.getReason(),
                entity.getCreatedAt()
        );
    }
}
