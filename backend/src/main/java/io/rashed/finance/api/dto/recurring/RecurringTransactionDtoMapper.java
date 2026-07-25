package io.rashed.finance.api.dto.recurring;

import io.rashed.finance.application.recurring.CreateRecurringTransactionCommand;
import io.rashed.finance.application.recurring.UpdateRecurringTransactionCommand;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionExecution;
import io.rashed.finance.domain.recurring.RecurringTransactionId;

import java.util.UUID;

public final class RecurringTransactionDtoMapper {

    private RecurringTransactionDtoMapper() {
    }

    public static CreateRecurringTransactionCommand toCommand(CreateRecurringTransactionRequest request) {

        return new CreateRecurringTransactionCommand(
                request.name(),
                request.transactionType(),
                request.fromAccountId() == null ? null : AccountId.of(request.fromAccountId()),
                request.toAccountId() == null ? null : AccountId.of(request.toAccountId()),
                request.categoryId() == null ? null : CategoryId.of(request.categoryId()),
                Money.of(request.amount()),
                request.frequency(),
                request.startDate(),
                request.endDate(),
                request.autoGenerate(),
                request.description(),
                request.notes()
        );
    }

    public static UpdateRecurringTransactionCommand toCommand(UUID id, UpdateRecurringTransactionRequest request) {

        return new UpdateRecurringTransactionCommand(
                RecurringTransactionId.of(id),
                request.name(),
                Money.of(request.amount()),
                request.frequency(),
                request.endDate(),
                request.autoGenerate(),
                request.description(),
                request.notes()
        );
    }

    public static RecurringTransactionResponse toResponse(RecurringTransaction recurringTransaction) {

        return new RecurringTransactionResponse(
                recurringTransaction.getId().getValue(),
                recurringTransaction.getName(),
                recurringTransaction.getTransactionType(),
                recurringTransaction.getFromAccountId() != null ? recurringTransaction.getFromAccountId().getValue() : null,
                recurringTransaction.getToAccountId() != null ? recurringTransaction.getToAccountId().getValue() : null,
                recurringTransaction.getCategoryId() != null ? recurringTransaction.getCategoryId().getValue() : null,
                recurringTransaction.getAmount().getAmount(),
                recurringTransaction.getFrequency(),
                recurringTransaction.getStartDate(),
                recurringTransaction.getEndDate(),
                recurringTransaction.getNextExecutionDate(),
                recurringTransaction.getLastExecutionDate(),
                recurringTransaction.isAutoGenerate(),
                recurringTransaction.isActive(),
                recurringTransaction.getDescription(),
                recurringTransaction.getNotes(),
                recurringTransaction.getCreatedAt(),
                recurringTransaction.getUpdatedAt()
        );
    }

    public static RecurringTransactionExecutionResponse toResponse(RecurringTransactionExecution execution) {

        return new RecurringTransactionExecutionResponse(
                execution.getId().getValue(),
                execution.getRecurringTransactionId().getValue(),
                execution.getScheduledDate(),
                execution.getStatus(),
                execution.getTransactionId() != null ? execution.getTransactionId().getValue() : null,
                execution.getReason(),
                execution.getCreatedAt()
        );
    }
}
