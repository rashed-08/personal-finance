package io.rashed.finance.domain.transactions;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Strongly typed identifier for Transaction aggregate.
 */
public final class TransactionId implements Serializable {

    private final UUID value;

    private TransactionId(UUID value) {
        this.value = Objects.requireNonNull(value, "Transaction ID cannot be null");
    }

    /**
     * Creates a new random transaction identifier.
     */
    public static TransactionId newId() {
        return new TransactionId(UUID.randomUUID());
    }

    /**
     * Creates a transaction identifier from an existing UUID.
     */
    public static TransactionId of(UUID value) {
        return new TransactionId(value);
    }

    /**
     * Creates a transaction identifier from a UUID string.
     */
    public static TransactionId of(String value) {
        return new TransactionId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionId that)) return false;
        return value.equals(that.value);
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