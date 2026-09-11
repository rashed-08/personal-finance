package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.domain.settings.Setting;
import io.rashed.finance.domain.settings.SettingsRepository;
import io.rashed.finance.infrastructure.persistence.entity.SettingEntity;
import io.rashed.finance.infrastructure.persistence.mapper.SettingEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.SettingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SettingsRepositoryImpl implements SettingsRepository {

    private final SettingJpaRepository jpaRepository;

    /**
     * The domain object carries a fresh {@link Setting#getId()} only when it
     * was just created; an updated setting keeps the id it was loaded with.
     * Looking the row up by key first keeps a save() on a detached domain
     * object from inserting a duplicate and tripping uk_settings_key.
     */
    @Override
    public Setting save(Setting setting) {

        SettingEntity entity = jpaRepository.findBySettingKey(setting.getKey())
                .map(existing -> new SettingEntity(
                        existing.getId(),
                        existing.getSettingKey(),
                        setting.getValue(),
                        setting.getValueType(),
                        setting.getDescription(),
                        setting.isSystemDefined(),
                        existing.getCreatedAt(),
                        setting.getUpdatedAt()
                ))
                .orElseGet(() -> SettingEntityMapper.toEntity(setting));

        return SettingEntityMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Setting> findByKey(String key) {

        return jpaRepository.findBySettingKey(key)
                .map(SettingEntityMapper::toDomain);
    }

    @Override
    public List<Setting> findAll() {

        return jpaRepository.findAllByOrderBySettingKeyAsc()
                .stream()
                .map(SettingEntityMapper::toDomain)
                .toList();
    }
}
