package io.rashed.finance.api.dto.settings;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;

/**
 * A batch of setting changes, applied all-or-nothing.
 *
 * A map rather than a fixed record because the key set is data, not
 * code: V5 adds five keys without this DTO changing. Values are text and
 * are validated against each setting's declared type by the domain.
 *
 * @param values key to new value. A null value unsets the setting, so
 *               readers fall back to their default.
 */
public record UpdateSettingsRequest(

        @NotEmpty(message = "At least one setting is required.")
        Map<String, String> values

) {
}
