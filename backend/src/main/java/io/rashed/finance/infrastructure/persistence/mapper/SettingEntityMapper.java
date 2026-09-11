package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.domain.settings.Setting;
import io.rashed.finance.domain.settings.SettingId;
import io.rashed.finance.infrastructure.persistence.entity.SettingEntity;

public final class SettingEntityMapper {

    private SettingEntityMapper() {
    }

    public static SettingEntity toEntity(Setting setting) {

        if (setting == null) {
            return null;
        }

        return new SettingEntity(
                setting.getId().getValue(),
                setting.getKey(),
                setting.getValue(),
                setting.getValueType(),
                setting.getDescription(),
                setting.isSystemDefined(),
                setting.getCreatedAt(),
                setting.getUpdatedAt()
        );
    }

    public static Setting toDomain(SettingEntity entity) {

        if (entity == null) {
            return null;
        }

        return new Setting(
                SettingId.of(entity.getId()),
                entity.getSettingKey(),
                entity.getSettingValue(),
                entity.getValueType(),
                entity.getDescription(),
                entity.isSystemDefined(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
