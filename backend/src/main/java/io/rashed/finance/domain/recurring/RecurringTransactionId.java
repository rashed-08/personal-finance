package io.rashed.finance.domain.recurring;

import io.rashed.finance.common.valueobject.EntityId;
import java.util.UUID;

/**
 * Strongly typed identifier for Recurring Transaction aggregate.
 */
public final class RecurringTransactionId extends EntityId {

    private RecurringTransactionId(UUID value) {
        super(value);
    }

    public static RecurringTransactionId newId() {
        return new RecurringTransactionId(UUID.randomUUID());
    }

    public static RecurringTransactionId of(UUID value) {
        return new RecurringTransactionId(value);
    }

    public static RecurringTransactionId of(String value) {
        return new RecurringTransactionId(UUID.fromString(value));
    }
}