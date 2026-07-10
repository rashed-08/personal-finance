package io.rashed.finance.domain.accounts;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Strongly typed identifier for Account aggregate.
 */
public final class AccountId implements Serializable {

    private final UUID value;

    private AccountId(UUID value) {
        this.value = Objects.requireNonNull(value, "Account ID cannot be null");
    }

    public static AccountId newId() {
        return new AccountId(UUID.randomUUID());
    }

    public static AccountId of(UUID value) {
        return new AccountId(value);
    }

    public static AccountId of(String value) {
        return new AccountId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof AccountId other && value.equals(other.value));
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