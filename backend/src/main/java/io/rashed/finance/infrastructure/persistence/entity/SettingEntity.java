package io.rashed.finance.infrastructure.persistence.entity;

import io.rashed.finance.common.enums.SettingValueType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "settings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settings_key",
                        columnNames = "setting_key"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettingEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "setting_key", nullable = false, length = 100, updatable = false)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "text")
    private String settingValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 20)
    private SettingValueType valueType;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "is_system", nullable = false)
    private boolean systemDefined;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public SettingEntity(
            UUID id,
            String settingKey,
            String settingValue,
            SettingValueType valueType,
            String description,
            boolean systemDefined,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.valueType = valueType;
        this.description = description;
        this.systemDefined = systemDefined;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
