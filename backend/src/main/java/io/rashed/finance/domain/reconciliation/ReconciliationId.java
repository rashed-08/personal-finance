package io.rashed.finance.domain.reconciliation;

import io.rashed.finance.common.valueobject.EntityId;

import java.util.UUID;

public final class ReconciliationId extends EntityId {

    private ReconciliationId(UUID value) {
        super(value);
    }

    public static ReconciliationId of(UUID value) {
        return new ReconciliationId(value);
    }

    public static ReconciliationId of(String value) {
        return new ReconciliationId(UUID.fromString(value));
    }

    public static ReconciliationId newId() {
        return new ReconciliationId(UUID.randomUUID());
    }
}