package io.rashed.finance.domain.salarycycle;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Strongly typed identifier for SalaryCycle aggregate.
 */
public final class SalaryCycleId implements Serializable {

    private final UUID value;

    private SalaryCycleId(UUID value) {
        this.value = Objects.requireNonNull(value, "Salary Cycle ID cannot be null");
    }

    public static SalaryCycleId newId() {
        return new SalaryCycleId(UUID.randomUUID());
    }

    public static SalaryCycleId of(UUID value) {
        return new SalaryCycleId(value);
    }

    public static SalaryCycleId of(String value) {
        return new SalaryCycleId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof SalaryCycleId other && value.equals(other.value));
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