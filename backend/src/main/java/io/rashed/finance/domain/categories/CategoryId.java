package io.rashed.finance.domain.categories;

import java.util.UUID;

import io.rashed.finance.common.valueobject.EntityId;

/**
 * Strongly typed identifier for Category aggregate.
 */
public final class CategoryId extends EntityId {

    private CategoryId(UUID value) {
        super(value);
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
}