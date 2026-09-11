package io.rashed.finance.domain.backup;

import java.util.UUID;

import io.rashed.finance.common.valueobject.EntityId;

/**
 * Strongly typed identifier for the BackupRecord aggregate.
 */
public final class BackupId extends EntityId {

    private BackupId(UUID value) {
        super(value);
    }

    public static BackupId newId() {
        return new BackupId(UUID.randomUUID());
    }

    public static BackupId of(UUID value) {
        return new BackupId(value);
    }

    public static BackupId of(String value) {
        return new BackupId(UUID.fromString(value));
    }
}
