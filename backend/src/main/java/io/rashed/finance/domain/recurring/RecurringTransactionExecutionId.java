package io.rashed.finance.domain.recurring;

import io.rashed.finance.common.valueobject.EntityId;
import java.util.UUID;

/**
 * Strongly typed identifier for a RecurringTransactionExecution log entry.
 */
public final class RecurringTransactionExecutionId extends EntityId {

    private RecurringTransactionExecutionId(UUID value) {
        super(value);
    }

    public static RecurringTransactionExecutionId newId() {
        return new RecurringTransactionExecutionId(UUID.randomUUID());
    }

    public static RecurringTransactionExecutionId of(UUID value) {
        return new RecurringTransactionExecutionId(value);
    }

    public static RecurringTransactionExecutionId of(String value) {
        return new RecurringTransactionExecutionId(UUID.fromString(value));
    }
}
