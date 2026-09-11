package io.rashed.finance.application.backup;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.rashed.finance.application.settings.SettingKeys;
import io.rashed.finance.application.settings.SettingsProvider;
import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupProvider;
import io.rashed.finance.common.enums.BackupStatus;
import io.rashed.finance.common.enums.BackupType;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.domain.backup.BackupRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateBackupServiceTest {

    private static final byte[] CONTENT = "ledger".getBytes(StandardCharsets.UTF_8);

    private BackupRepository backupRepository;
    private BackupExporter exporter;
    private BackupImporter importer;
    private BackupStorage storage;
    private SettingsProvider settings;
    private CreateBackupService service;

    @BeforeEach
    void setUp() {

        backupRepository = mock(BackupRepository.class);
        exporter = mock(BackupExporter.class);
        importer = mock(BackupImporter.class);
        storage = mock(BackupStorage.class);
        settings = mock(SettingsProvider.class);

        when(exporter.format()).thenReturn(BackupFormat.JSON);
        when(importer.format()).thenReturn(BackupFormat.JSON);
        when(storage.provider()).thenReturn(BackupProvider.LOCAL);
        when(storage.isAvailable()).thenReturn(true);

        when(exporter.export(anyString())).thenAnswer(invocation ->
                BackupArchive.of(invocation.getArgument(0), BackupFormat.JSON, CONTENT));

        when(storage.store(any())).thenReturn(
                new BackupStorage.StoredArchive("/backups/archive.zip", null));

        // Saving returns whatever was handed in, as the real repository does.
        when(backupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service = new CreateBackupService(
                backupRepository,
                new BackupFormats(List.of(exporter), List.of(importer)),
                new BackupStorages(List.of(storage)),
                settings
        );
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void execute_exportsStoresAndRecordsTheBackup() {

        BackupRecord record = service.execute(
                BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.JSON);

        assertTrue(record.isCompleted());
        assertEquals("/backups/archive.zip", record.getFilePath());
        assertEquals(CONTENT.length, record.getFileSize());
        assertEquals(BackupArchive.sha256(CONTENT), record.getChecksum());

        verify(exporter).export(record.getFileName());
        verify(storage).store(any());
    }

    @Test
    void execute_opensTheHistoryRecordBeforeDoingTheWork() {

        service.execute(BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.JSON);

        ArgumentCaptor<BackupRecord> saved = ArgumentCaptor.forClass(BackupRecord.class);
        verify(backupRepository, org.mockito.Mockito.times(2)).save(saved.capture());

        // A crash between the two saves must leave evidence, so the first
        // one has to happen before the export.
        assertEquals(BackupStatus.IN_PROGRESS, saved.getAllValues().getFirst().getStatus());
        assertEquals(BackupStatus.COMPLETED, saved.getAllValues().getLast().getStatus());
    }

    // -------------------------------------------------------------------------
    // Failures
    // -------------------------------------------------------------------------

    @Test
    void execute_recordsAFailedExportAndRethrows() {

        // doThrow, not when(...).thenThrow: re-stubbing through when()
        // would evaluate the existing stub with the matcher's placeholder
        // argument and blow up inside setUp's answer.
        doThrow(new BackupFailedException("Could not read table transactions"))
                .when(exporter).export(anyString());

        assertThrows(
                BackupFailedException.class,
                () -> service.execute(BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.JSON)
        );

        ArgumentCaptor<BackupRecord> saved = ArgumentCaptor.forClass(BackupRecord.class);
        verify(backupRepository, org.mockito.Mockito.times(2)).save(saved.capture());

        BackupRecord failed = saved.getAllValues().getLast();

        assertTrue(failed.isFailed());
        assertEquals("Could not read table transactions", failed.getErrorMessage());
    }

    @Test
    void execute_recordsAFailedUpload() {

        doThrow(new BackupFailedException("Drive quota exceeded"))
                .when(storage).store(any());

        assertThrows(
                BackupFailedException.class,
                () -> service.execute(BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.JSON)
        );

        ArgumentCaptor<BackupRecord> saved = ArgumentCaptor.forClass(BackupRecord.class);
        verify(backupRepository, org.mockito.Mockito.times(2)).save(saved.capture());

        assertTrue(saved.getAllValues().getLast().isFailed());
    }

    @Test
    void execute_refusesAnUnavailableProviderWithoutWritingHistory() {

        when(storage.isAvailable()).thenReturn(false);
        when(storage.unavailableReason()).thenReturn("Google Drive is not connected.");

        BackupFailedException thrown = assertThrows(
                BackupFailedException.class,
                () -> service.execute(BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.JSON)
        );

        assertEquals("Google Drive is not connected.", thrown.getMessage());

        // An unconfigured provider is not a failed attempt, so nothing is
        // recorded and no archive is produced.
        verify(backupRepository, never()).save(any());
        verify(exporter, never()).export(anyString());
    }

    @Test
    void execute_refusesAProviderWithNoImplementation() {

        BackupFailedException thrown = assertThrows(
                BackupFailedException.class,
                () -> service.execute(BackupType.MANUAL, BackupProvider.S3, BackupFormat.JSON)
        );

        assertTrue(thrown.getMessage().contains("S3"));
        verify(backupRepository, never()).save(any());
    }

    @Test
    void execute_refusesAFormatWithNoExporter() {

        assertThrows(
                BackupFailedException.class,
                () -> service.execute(BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.PG_DUMP)
        );
    }

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    @Test
    void configuredProviderAndFormat_comeFromSettings() {

        when(settings.getEnum(SettingKeys.BACKUP_PROVIDER, BackupProvider.class, BackupProvider.LOCAL))
                .thenReturn(BackupProvider.GOOGLE_DRIVE);

        when(settings.getEnum(SettingKeys.BACKUP_FORMAT, BackupFormat.class, BackupFormat.JSON))
                .thenReturn(BackupFormat.PG_DUMP);

        assertEquals(BackupProvider.GOOGLE_DRIVE, service.configuredProvider());
        assertEquals(BackupFormat.PG_DUMP, service.configuredFormat());
    }

    @Test
    void execute_withoutOverridesUsesTheConfiguredProviderAndFormat() {

        when(settings.getEnum(SettingKeys.BACKUP_PROVIDER, BackupProvider.class, BackupProvider.LOCAL))
                .thenReturn(BackupProvider.LOCAL);

        when(settings.getEnum(SettingKeys.BACKUP_FORMAT, BackupFormat.class, BackupFormat.JSON))
                .thenReturn(BackupFormat.JSON);

        BackupRecord record = service.execute(BackupType.AUTOMATIC);

        assertTrue(record.isCompleted());
        assertTrue(record.isAutomatic());
        assertEquals(BackupProvider.LOCAL, record.getProvider());
        assertEquals(BackupFormat.JSON, record.getFormat());
    }

    @Test
    void execute_namesTheArchiveForItsFormat() {

        BackupRecord record = service.execute(
                BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.JSON);

        assertTrue(record.getFileName().startsWith("finance-backup-"));
        assertTrue(record.getFileName().endsWith(".zip"));
    }
}
