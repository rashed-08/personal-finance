package io.rashed.finance.domain.users;

import io.rashed.finance.common.valueobject.EntityId;
import java.util.UUID;

/**
 * Strongly typed identifier for EmailVerificationToken aggregate.
 */
public final class EmailVerificationTokenId extends EntityId {

    private EmailVerificationTokenId(UUID value) {
        super(value);
    }

    public static EmailVerificationTokenId newId() {
        return new EmailVerificationTokenId(UUID.randomUUID());
    }

    public static EmailVerificationTokenId of(UUID value) {
        return new EmailVerificationTokenId(value);
    }
}
