package io.rashed.finance.domain.settings;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import io.rashed.finance.common.enums.SettingValueType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * One application configuration entry.
 *
 * Settings are key-value rather than typed columns (see
 * docs/database/tables/settings.md): the value is stored as text and
 * {@link #getValueType()} says how to read it. This aggregate owns that
 * contract — a value is parsed and validated against its declared type
 * here, so a malformed value is rejected at the boundary rather than
 * blowing up whichever module later reads it.
 *
 * Settings hold configuration only, never financial data, so changes
 * affect future behaviour and never rewrite history.
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public final class Setting {

    private static final int MAX_KEY_LENGTH = 100;

    private final SettingId id;

    /**
     * Stable identifier used by code, e.g. {@code DEFAULT_CURRENCY}.
     * Upper snake case; never renamed once released, since a rename would
     * silently fall back to the caller's default.
     */
    private final String key;

    /**
     * Raw stored text. Null means "explicitly unset" and callers fall back
     * to their own default.
     */
    private final String value;

    private final SettingValueType valueType;

    private final String description;

    /**
     * System settings are seeded by migration and understood by code.
     * They may be edited but never deleted, so a missing key can always be
     * treated as a bug rather than a user action.
     */
    private final boolean systemDefined;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public Setting(
            SettingId id,
            String key,
            String value,
            SettingValueType valueType,
            String description,
            boolean systemDefined,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        this.id = Objects.requireNonNull(id);
        this.key = normalizeKey(key);
        this.valueType = Objects.requireNonNull(valueType, "Setting value type cannot be null.");

        this.value = normalizeValue(value, this.valueType);

        this.description = description;
        this.systemDefined = systemDefined;

        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    public static Setting create(
            String key,
            String value,
            SettingValueType valueType,
            String description
    ) {

        LocalDateTime now = LocalDateTime.now();

        return new Setting(SettingId.newId(), key, value, valueType, description, false, now, now);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private static String normalizeKey(String key) {

        Objects.requireNonNull(key, "Setting key cannot be null.");

        String trimmed = key.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Setting key cannot be empty.");
        }

        if (trimmed.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException(
                    "Setting key cannot exceed " + MAX_KEY_LENGTH + " characters.");
        }

        return trimmed;
    }

    /**
     * Blank is stored as null so "unset" has exactly one representation;
     * anything else must parse as its declared type.
     */
    private static String normalizeValue(String value, SettingValueType valueType) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();

        validateAgainstType(trimmed, valueType);

        return trimmed;
    }

    private static void validateAgainstType(String value, SettingValueType valueType) {

        switch (valueType) {

            case INTEGER -> parseOrReject(value, valueType, Long::parseLong);

            case DECIMAL -> parseOrReject(value, valueType, BigDecimal::new);

            case BOOLEAN -> {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException(
                            "Setting value '" + value + "' is not a valid BOOLEAN (expected true or false).");
                }
            }

            case DATE -> parseOrReject(value, valueType, LocalDate::parse);

            // A JSON value is stored verbatim: the shape is the concern of
            // whichever module defined the key, not of this aggregate.
            case STRING, JSON -> {
            }
        }
    }

    private static void parseOrReject(
            String value,
            SettingValueType valueType,
            java.util.function.Function<String, ?> parser
    ) {

        try {
            parser.apply(value);

        } catch (NumberFormatException | DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Setting value '" + value + "' is not a valid " + valueType + ".");
        }
    }

    // -------------------------------------------------------------------------
    // Business Methods
    // -------------------------------------------------------------------------

    public boolean isSystemDefined() {
        return systemDefined;
    }

    public boolean hasValue() {
        return value != null;
    }

    /**
     * Replaces the stored value, validating it against the declared type.
     * The type itself is never changed by an update: code reads this key
     * expecting a particular type, so switching it is a migration.
     */
    public Setting changeValue(String newValue) {

        return new Setting(
                id, key, newValue, valueType, description, systemDefined, createdAt, LocalDateTime.now());
    }

    // -------------------------------------------------------------------------
    // Typed Readers
    // -------------------------------------------------------------------------

    /**
     * @throws IllegalStateException if the declared type is not BOOLEAN —
     *         reading a key as the wrong type is a programming error, not
     *         bad input.
     */
    public boolean asBoolean(boolean defaultValue) {

        requireType(SettingValueType.BOOLEAN);

        return hasValue() ? Boolean.parseBoolean(value) : defaultValue;
    }

    public long asLong(long defaultValue) {

        requireType(SettingValueType.INTEGER);

        return hasValue() ? Long.parseLong(value) : defaultValue;
    }

    public int asInt(int defaultValue) {

        return Math.toIntExact(asLong(defaultValue));
    }

    public BigDecimal asDecimal(BigDecimal defaultValue) {

        requireType(SettingValueType.DECIMAL);

        return hasValue() ? new BigDecimal(value) : defaultValue;
    }

    public LocalDate asDate(LocalDate defaultValue) {

        requireType(SettingValueType.DATE);

        return hasValue() ? LocalDate.parse(value) : defaultValue;
    }

    public String asString(String defaultValue) {

        return hasValue() ? value : defaultValue;
    }

    /**
     * Reads the value as an enum constant, case-insensitively. An
     * unrecognised constant falls back to the default rather than throwing:
     * a stale value in the database must not stop the application booting.
     */
    public <E extends Enum<E>> E asEnum(Class<E> type, E defaultValue) {

        Objects.requireNonNull(type, "Enum type cannot be null.");

        if (!hasValue()) {
            return defaultValue;
        }

        for (E constant : type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(value)) {
                return constant;
            }
        }

        return defaultValue;
    }

    private void requireType(SettingValueType expected) {

        if (valueType != expected) {
            throw new IllegalStateException(
                    "Setting " + key + " is declared " + valueType + ", not " + expected + ".");
        }
    }
}
