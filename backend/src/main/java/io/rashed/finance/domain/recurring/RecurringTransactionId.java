package io.rashed.finance.domain.recurring;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Strongly typed identifier for Recurring Transaction aggregate.
 */
public final class RecurringTransactionId implements Serializable {

    private final UUID value;

    private RecurringTransactionId(UUID value) {
        this.value = Objects.requireNonNull(value, "Recurring Transaction ID cannot be null");
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

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof RecurringTransactionId other && value.equals(other.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}