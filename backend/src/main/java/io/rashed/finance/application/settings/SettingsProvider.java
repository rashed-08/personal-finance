package io.rashed.finance.application.settings;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.rashed.finance.domain.settings.Setting;
import io.rashed.finance.domain.settings.SettingsRepository;

/**
 * Typed read access to configuration.
 *
 * Every module that needs a setting goes through here rather than reading
 * the key-value table directly, so that:
 *
 * <ul>
 *   <li>a missing or blank key falls back to the caller's default instead
 *       of throwing — configuration must never stop a feature booting;</li>
 *   <li>the text-to-type conversion lives in one place
 *       ({@link Setting}), not at every call site.</li>
 * </ul>
 *
 * Reads hit the database each time. That is deliberate: settings change
 * rarely but must take effect immediately, and the table holds a handful
 * of rows behind a unique index. Caching here would mean an invalidation
 * problem in exchange for nothing measurable.
 */
@Service
public class SettingsProvider {

    private final SettingsRepository repository;

    public SettingsProvider(SettingsRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public String getString(String key, String defaultValue) {

        return find(key)
                .map(setting -> setting.asString(defaultValue))
                .orElse(defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {

        return find(key)
                .map(setting -> setting.asBoolean(defaultValue))
                .orElse(defaultValue);
    }

    public int getInt(String key, int defaultValue) {

        return find(key)
                .map(setting -> setting.asInt(defaultValue))
                .orElse(defaultValue);
    }

    public long getLong(String key, long defaultValue) {

        return find(key)
                .map(setting -> setting.asLong(defaultValue))
                .orElse(defaultValue);
    }

    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {

        return find(key)
                .map(setting -> setting.asDecimal(defaultValue))
                .orElse(defaultValue);
    }

    public <E extends Enum<E>> E getEnum(String key, Class<E> type, E defaultValue) {

        return find(key)
                .map(setting -> setting.asEnum(type, defaultValue))
                .orElse(defaultValue);
    }

    private Optional<Setting> find(String key) {

        Objects.requireNonNull(key, "Setting key cannot be null.");

        return repository.findByKey(key);
    }
}
