package io.rashed.finance.domain.transactions;

import java.util.UUID;
import io.rashed.finance.common.valueobject.EntityId;

/**
 * Strongly typed identifier for Transaction aggregate.
 */
public final class TransactionId extends EntityId {

    private TransactionId(UUID value) {
        super(value);
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
}