package io.rashed.finance.domain.settings;

import java.util.List;
import java.util.Optional;

public interface SettingsRepository {

    Setting save(Setting setting);

    Optional<Setting> findByKey(String key);

    List<Setting> findAll();
}
