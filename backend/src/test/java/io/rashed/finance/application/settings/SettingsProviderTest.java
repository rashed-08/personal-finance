package io.rashed.finance.application.settings;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.rashed.finance.common.enums.BackupProvider;
import io.rashed.finance.common.enums.SettingValueType;
import io.rashed.finance.domain.settings.Setting;
import io.rashed.finance.domain.settings.SettingsRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SettingsProviderTest {

    private SettingsRepository repository;
    private SettingsProvider provider;

    @BeforeEach
    void setUp() {

        repository = mock(SettingsRepository.class);

        // Default: nothing is configured, so every read must fall back.
        when(repository.findByKey(anyString())).thenReturn(Optional.empty());

        provider = new SettingsProvider(repository);
    }

    private void given(String key, String value, SettingValueType type) {

        when(repository.findByKey(key))
                .thenReturn(Optional.of(Setting.create(key, value, type, null)));
    }

    // -------------------------------------------------------------------------
    // Missing keys
    // -------------------------------------------------------------------------

    @Test
    void aMissingKeyFallsBackToTheCallerDefault() {

        // Configuration must never stop a feature working.
        assertEquals("BDT", provider.getString(SettingKeys.DEFAULT_CURRENCY, "BDT"));
        assertFalse(provider.getBoolean(SettingKeys.AUTO_BACKUP_ENABLED, false));
        assertEquals(30, provider.getInt(SettingKeys.BACKUP_RETENTION_COUNT, 30));
        assertEquals(30L, provider.getLong(SettingKeys.BACKUP_RETENTION_COUNT, 30L));
        assertEquals(
                BigDecimal.ONE,
                provider.getDecimal("SOME_RATE", BigDecimal.ONE)
        );
        assertEquals(
                BackupProvider.LOCAL,
                provider.getEnum(SettingKeys.BACKUP_PROVIDER, BackupProvider.class, BackupProvider.LOCAL)
        );
    }

    @Test
    void aKeyPresentButUnsetAlsoFallsBack() {

        given(SettingKeys.BACKUP_DIRECTORY, null, SettingValueType.STRING);

        assertEquals(
                "storage/backups",
                provider.getString(SettingKeys.BACKUP_DIRECTORY, "storage/backups")
        );
    }

    // -------------------------------------------------------------------------
    // Configured keys
    // -------------------------------------------------------------------------

    @Test
    void readsAConfiguredValue() {

        given(SettingKeys.DEFAULT_CURRENCY, "USD", SettingValueType.STRING);
        given(SettingKeys.AUTO_BACKUP_ENABLED, "true", SettingValueType.BOOLEAN);
        given(SettingKeys.BACKUP_RETENTION_COUNT, "7", SettingValueType.INTEGER);
        given(SettingKeys.BACKUP_PROVIDER, "GOOGLE_DRIVE", SettingValueType.STRING);

        assertEquals("USD", provider.getString(SettingKeys.DEFAULT_CURRENCY, "BDT"));
        assertTrue(provider.getBoolean(SettingKeys.AUTO_BACKUP_ENABLED, false));
        assertEquals(7, provider.getInt(SettingKeys.BACKUP_RETENTION_COUNT, 30));
        assertEquals(
                BackupProvider.GOOGLE_DRIVE,
                provider.getEnum(SettingKeys.BACKUP_PROVIDER, BackupProvider.class, BackupProvider.LOCAL)
        );
    }

    @Test
    void theSeededDefaultsAreReadableAtTheirDeclaredTypes() {

        // Mirrors what V2/V5 seed, so a type mismatch between the
        // migration and the code shows up here.
        given(SettingKeys.AUTO_BACKUP_ENABLED, "false", SettingValueType.BOOLEAN);
        given(SettingKeys.BACKUP_PROVIDER, "LOCAL", SettingValueType.STRING);
        given(SettingKeys.BACKUP_FORMAT, "JSON", SettingValueType.STRING);
        given(SettingKeys.BACKUP_DIRECTORY, "storage/backups", SettingValueType.STRING);
        given(SettingKeys.BACKUP_RETENTION_COUNT, "30", SettingValueType.INTEGER);
        given(SettingKeys.GOOGLE_DRIVE_FOLDER_NAME, "PersonalFinanceApp", SettingValueType.STRING);

        assertFalse(provider.getBoolean(SettingKeys.AUTO_BACKUP_ENABLED, true));
        assertEquals(
                BackupProvider.LOCAL,
                provider.getEnum(SettingKeys.BACKUP_PROVIDER, BackupProvider.class, BackupProvider.GOOGLE_DRIVE)
        );
        assertEquals(30, provider.getInt(SettingKeys.BACKUP_RETENTION_COUNT, 1));
        assertEquals(
                "PersonalFinanceApp",
                provider.getString(SettingKeys.GOOGLE_DRIVE_FOLDER_NAME, "x")
        );
    }
}
