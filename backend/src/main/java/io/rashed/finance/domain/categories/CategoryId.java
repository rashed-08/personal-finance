package io.rashed.finance.domain.categories;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Strongly typed identifier for Category aggregate.
 */
public final class CategoryId implements Serializable {

    private final UUID value;

    private CategoryId(UUID value) {
        this.value = Objects.requireNonNull(value, "Category ID cannot be null");
    }

    public static CategoryId newId() {
        return new CategoryId(UUID.randomUUID());
    }

    public static CategoryId of(UUID value) {
        return new CategoryId(value);
    }

    public static CategoryId of(String value) {
        return new CategoryId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof CategoryId other && value.equals(other.value));
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