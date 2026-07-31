package io.rashed.finance.domain.users;

import io.rashed.finance.common.valueobject.EntityId;
import java.util.UUID;

/**
 * Strongly typed identifier for PasswordResetToken aggregate.
 */
public final class PasswordResetTokenId extends EntityId {

    private PasswordResetTokenId(UUID value) {
        super(value);
    }

    public static PasswordResetTokenId newId() {
        return new PasswordResetTokenId(UUID.randomUUID());
    }

    public static PasswordResetTokenId of(UUID value) {
        return new PasswordResetTokenId(value);
    }
}
