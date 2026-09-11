package io.rashed.finance.application.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.settings.Setting;
import io.rashed.finance.domain.settings.SettingsRepository;

/**
 * Applies a batch of setting changes.
 *
 * Not final: {@code @Transactional} is applied through a CGLIB proxy,
 * which subclasses this type.
 *
 * The batch is all-or-nothing. The settings screen submits several keys at
 * once and a partial apply would leave a configuration nobody chose — for
 * example auto-backup switched on while the provider it needs was rejected.
 */
@Service
public class UpdateSettingsService {

    private final SettingsRepository repository;

    public UpdateSettingsService(SettingsRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    /**
     * @param values key to new raw value; a null or blank value unsets the
     *               setting, so readers fall back to their default.
     * @return the updated settings, in the order given.
     * @throws ResourceNotFoundException if a key does not exist. Keys are
     *         seeded by migration, so an unknown one is a client bug rather
     *         than a reason to create a row no code will ever read.
     */
    @Transactional
    public List<Setting> execute(Map<String, String> values) {

        Objects.requireNonNull(values, "Values cannot be null.");

        List<Setting> updated = new ArrayList<>(values.size());

        for (Map.Entry<String, String> entry : values.entrySet()) {

            Setting setting = repository.findByKey(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Unknown setting: " + entry.getKey()));

            updated.add(repository.save(setting.changeValue(entry.getValue())));
        }

        return updated;
    }
}
