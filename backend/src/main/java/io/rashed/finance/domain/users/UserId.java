package io.rashed.finance.domain.users;

import io.rashed.finance.common.valueobject.EntityId;
import java.util.UUID;

/**
 * Strongly typed identifier for User aggregate.
 */
public final class UserId extends EntityId {

    private UserId(UUID value) {
        super(value);
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId of(String value) {
        return new UserId(UUID.fromString(value));
    }
}
