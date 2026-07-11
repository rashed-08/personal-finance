package io.rashed.finance.domain.funds;

import java.util.UUID;

import io.rashed.finance.common.valueobject.EntityId;

/**
 * Strongly typed identifier for Fund aggregate.
 */
public final class FundId extends EntityId {

    private FundId(UUID value) {
        super(value);
    }

    public static FundId newId() {
        return new FundId(UUID.randomUUID());
    }

    public static FundId of(UUID value) {
        return new FundId(value);
    }

    public static FundId of(String value) {
        return new FundId(UUID.fromString(value));
    }
}