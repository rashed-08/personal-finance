package io.rashed.finance.domain.settings;

import java.util.UUID;

import io.rashed.finance.common.valueobject.EntityId;

/**
 * Strongly typed identifier for the Setting aggregate.
 */
public final class SettingId extends EntityId {

    private SettingId(UUID value) {
        super(value);
    }

    public static SettingId newId() {
        return new SettingId(UUID.randomUUID());
    }

    public static SettingId of(UUID value) {
        return new SettingId(value);
    }

    public static SettingId of(String value) {
        return new SettingId(UUID.fromString(value));
    }
}
