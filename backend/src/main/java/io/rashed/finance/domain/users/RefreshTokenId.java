package io.rashed.finance.domain.users;

import io.rashed.finance.common.valueobject.EntityId;
import java.util.UUID;

/**
 * Strongly typed identifier for RefreshToken aggregate.
 */
public final class RefreshTokenId extends EntityId {

    private RefreshTokenId(UUID value) {
        super(value);
    }

    public static RefreshTokenId newId() {
        return new RefreshTokenId(UUID.randomUUID());
    }

    public static RefreshTokenId of(UUID value) {
        return new RefreshTokenId(value);
    }
}
