package io.rashed.finance.application.settings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.rashed.finance.common.enums.SettingValueType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.settings.Setting;
import io.rashed.finance.domain.settings.SettingsRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateSettingsServiceTest {

    private SettingsRepository repository;
    private UpdateSettingsService service;

    @BeforeEach
    void setUp() {

        repository = mock(SettingsRepository.class);

        when(repository.findByKey(anyString())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service = new UpdateSettingsService(repository);
    }

    private void given(String key, String value, SettingValueType type) {

        when(repository.findByKey(key))
                .thenReturn(Optional.of(Setting.create(key, value, type, null)));
    }

    @Test
    void execute_appliesEveryChangeInTheBatch() {

        given(SettingKeys.AUTO_BACKUP_ENABLED, "false", SettingValueType.BOOLEAN);
        given(SettingKeys.BACKUP_PROVIDER, "LOCAL", SettingValueType.STRING);

        Map<String, String> values = new LinkedHashMap<>();
        values.put(SettingKeys.AUTO_BACKUP_ENABLED, "true");
        values.put(SettingKeys.BACKUP_PROVIDER, "GOOGLE_DRIVE");

        List<Setting> updated = service.execute(values);

        assertEquals(2, updated.size());
        assertEquals("true", updated.getFirst().getValue());
        assertEquals("GOOGLE_DRIVE", updated.getLast().getValue());

        verify(repository, org.mockito.Mockito.times(2)).save(any());
    }

    @Test
    void execute_rejectsAnUnknownKey() {

        // Keys are seeded by migration; an unknown one is a client bug, not
        // a reason to create a row no code will read.
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.execute(Map.of("NOT_A_SETTING", "x"))
        );

        verify(repository, never()).save(any());
    }

    @Test
    void execute_rejectsAValueThatDoesNotMatchTheDeclaredType() {

        given(SettingKeys.BACKUP_RETENTION_COUNT, "30", SettingValueType.INTEGER);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.execute(Map.of(SettingKeys.BACKUP_RETENTION_COUNT, "loads"))
        );
    }

    @Test
    void execute_canUnsetASetting() {

        given(SettingKeys.BACKUP_DIRECTORY, "storage/backups", SettingValueType.STRING);

        Map<String, String> values = new LinkedHashMap<>();
        values.put(SettingKeys.BACKUP_DIRECTORY, null);

        assertFalse(service.execute(values).getFirst().hasValue());
    }
}
