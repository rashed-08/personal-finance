package io.rashed.finance.domain.accounts;

import io.rashed.finance.common.valueobject.EntityId;
import java.util.UUID;

/**
 * Strongly typed identifier for Account aggregate.
 */
public final class AccountId extends EntityId {

    private AccountId(UUID value) {
        super(value);
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
}