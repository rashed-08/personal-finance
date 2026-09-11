package io.rashed.finance.domain.users;

import java.util.UUID;

import io.rashed.finance.common.valueobject.EntityId;

/**
 * Strongly typed identifier for the GoogleOAuthToken aggregate.
 */
public final class GoogleOAuthTokenId extends EntityId {

    private GoogleOAuthTokenId(UUID value) {
        super(value);
    }

    public static GoogleOAuthTokenId newId() {
        return new GoogleOAuthTokenId(UUID.randomUUID());
    }

    public static GoogleOAuthTokenId of(UUID value) {
        return new GoogleOAuthTokenId(value);
    }

    public static GoogleOAuthTokenId of(String value) {
        return new GoogleOAuthTokenId(UUID.fromString(value));
    }
}
