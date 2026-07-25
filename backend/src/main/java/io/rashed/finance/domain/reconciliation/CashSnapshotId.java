package io.rashed.finance.domain.reconciliation;

import io.rashed.finance.common.valueobject.EntityId;

import java.util.UUID;

/**
 * Strongly typed identifier for a CashSnapshot child entity.
 */
public final class CashSnapshotId extends EntityId {

    private CashSnapshotId(UUID value) {
        super(value);
    }

    public static CashSnapshotId newId() {
        return new CashSnapshotId(UUID.randomUUID());
    }

    public static CashSnapshotId of(UUID value) {
        return new CashSnapshotId(value);
    }

    public static CashSnapshotId of(String value) {
        return new CashSnapshotId(UUID.fromString(value));
    }
}
