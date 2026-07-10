package io.rashed.finance.domain.funds;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Strongly typed identifier for Fund aggregate.
 */
public final class FundId implements Serializable {

    private final UUID value;

    private FundId(UUID value) {
        this.value = Objects.requireNonNull(value, "Fund ID cannot be null");
    }

    public static FundId newId() {
        return new FundId(UUID.randomUUID());
    }

    public static FundId of(UUID value) {
        return new FundId(value);
    }

    public static FundId of(String value) {
        return new FundId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof FundId other && value.equals(other.value));
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