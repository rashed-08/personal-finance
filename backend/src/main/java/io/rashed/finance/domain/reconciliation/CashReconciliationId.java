package io.rashed.finance.domain.reconciliation;

import io.rashed.finance.common.valueobject.EntityId;

import java.util.UUID;

/**
 * Strongly typed identifier for the CashReconciliation aggregate.
 */
public final class CashReconciliationId extends EntityId {

    private CashReconciliationId(UUID value) {
        super(value);
    }

    public static CashReconciliationId newId() {
        return new CashReconciliationId(UUID.randomUUID());
    }

    public static CashReconciliationId of(UUID value) {
        return new CashReconciliationId(value);
    }

    public static CashReconciliationId of(String value) {
        return new CashReconciliationId(UUID.fromString(value));
    }
}
