package io.rashed.finance.domain.loans;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Strongly typed identifier for Loan aggregate.
 */
public final class LoanId implements Serializable {

    private final UUID value;

    private LoanId(UUID value) {
        this.value = Objects.requireNonNull(value, "Loan ID cannot be null");
    }

    public static LoanId newId() {
        return new LoanId(UUID.randomUUID());
    }

    public static LoanId of(UUID value) {
        return new LoanId(value);
    }

    public static LoanId of(String value) {
        return new LoanId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof LoanId other && value.equals(other.value));
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