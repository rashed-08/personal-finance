package io.rashed.finance.api.dto.settings;

import io.rashed.finance.domain.settings.Setting;

public final class SettingsDtoMapper {

    private SettingsDtoMapper() {
    }

    public static SettingResponse toResponse(Setting setting) {

        return new SettingResponse(
                setting.getKey(),
                setting.getValue(),
                setting.getValueType(),
                setting.getDescription(),
                setting.isSystemDefined(),
                setting.getUpdatedAt()
        );
    }
}
