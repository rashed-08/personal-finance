package io.rashed.finance.api.dto.settings;

import java.time.LocalDateTime;

import io.rashed.finance.common.enums.SettingValueType;

/**
 * One configuration entry.
 *
 * @param key         stable identifier, e.g. {@code DEFAULT_CURRENCY}.
 * @param value       stored value as text, or null when unset.
 * @param valueType   how to interpret {@code value}; drives the form
 *                    control the UI renders.
 * @param description what the setting does, shown as help text.
 * @param system      seeded by migration and understood by code. The UI
 *                    may edit these but must not offer to delete them.
 */
public record SettingResponse(
        String key,
        String value,
        SettingValueType valueType,
        String description,
        boolean system,
        LocalDateTime updatedAt
) {
}
