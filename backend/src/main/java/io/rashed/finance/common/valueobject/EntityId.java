package io.rashed.finance.common.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Base class for all strongly-typed Entity IDs.
 *
 * Examples:
 *  - AccountId
 *  - TransactionId
 *  - CategoryId
 *  - SalaryCycleId
 *
 * Each domain ID should extend this class.
 */
public abstract class EntityId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID value;

    protected EntityId(UUID value) {
        this.value = Objects.requireNonNull(value, "Entity ID cannot be null");
    }

    public UUID getValue() {
        return value;
    }

    public String asString() {
        return value.toString();
    }

    public boolean isEmpty() {
        return value.equals(new UUID(0L, 0L));
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        EntityId other = (EntityId) obj;
        return value.equals(other.value);
    }

    @Override
    public final int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}