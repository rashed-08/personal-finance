package io.rashed.finance.application.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.rashed.finance.application.settings.SettingKeys;
import io.rashed.finance.application.settings.SettingsProvider;
import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupProvider;
import io.rashed.finance.common.enums.BackupType;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.domain.backup.BackupRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RunScheduledBackupServiceTest {

    private CreateBackupService createBackupService;
    private DeleteBackupService deleteBackupService;
    private BackupRepository backupRepository;
    private SettingsProvider settings;
    private RunScheduledBackupService service;

    @BeforeEach
    void setUp() {

        createBackupService = mock(CreateBackupService.class);
        deleteBackupService = mock(DeleteBackupService.class);
        backupRepository = mock(BackupRepository.class);
        settings = mock(SettingsProvider.class);

        when(createBackupService.execute(any())).thenReturn(completedBackup(0));
        when(backupRepository.findCompletedAutomaticBackups()).thenReturn(List.of());

        when(settings.getBoolean(eq(SettingKeys.AUTO_BACKUP_ENABLED), anyBoolean()))
                .thenReturn(true);
        when(settings.getInt(eq(SettingKeys.BACKUP_RETENTION_COUNT), anyInt()))
                .thenReturn(3);

        service = new RunScheduledBackupService(
                createBackupService, deleteBackupService, backupRepository, settings);
    }

    private static BackupRecord completedBackup(int index) {

        return BackupRecord
                .startBackup(
                        BackupType.AUTOMATIC,
                        BackupProvider.LOCAL,
                        BackupFormat.JSON,
                        "finance-backup-" + index + ".zip"
                )
                .complete("/backups/" + index + ".zip", null, 100L, "checksum" + index);
    }

    /**
     * Newest first, as the repository returns them.
     */
    private static List<BackupRecord> backups(int count) {

        List<BackupRecord> records = new ArrayList<>();

        IntStream.range(0, count).forEach(index -> records.add(completedBackup(index)));

        return records;
    }

    // -------------------------------------------------------------------------
    // The schedule gate
    // -------------------------------------------------------------------------

    @Test
    void runScheduled_doesNothingWhenAutoBackupIsOff() {

        when(settings.getBoolean(eq(SettingKeys.AUTO_BACKUP_ENABLED), anyBoolean()))
                .thenReturn(false);

        service.runScheduled();

        verify(createBackupService, never()).execute(any());
    }

    @Test
    void runScheduled_takesAnAutomaticBackupWhenEnabled() {

        service.runScheduled();

        verify(createBackupService).execute(BackupType.AUTOMATIC);
    }

    @Test
    void runScheduled_swallowsAFailure() {

        // The run already recorded itself as FAILED; letting the exception
        // escape a scheduled method only adds a second stack trace.
        doThrow(new BackupFailedException("Disk full"))
                .when(createBackupService).execute(any());

        service.runScheduled();

        verify(createBackupService).execute(BackupType.AUTOMATIC);
    }

    // -------------------------------------------------------------------------
    // Retention
    // -------------------------------------------------------------------------

    @Test
    void execute_prunesArchivesBeyondTheRetentionCount() {

        when(backupRepository.findCompletedAutomaticBackups()).thenReturn(backups(5));

        service.execute();

        // Keep the newest 3, prune the other 2.
        verify(deleteBackupService, times(2)).deleteArchive(any());
    }

    @Test
    void execute_keepsEverythingWhenWithinRetention() {

        when(backupRepository.findCompletedAutomaticBackups()).thenReturn(backups(3));

        service.execute();

        verify(deleteBackupService, never()).deleteArchive(any());
    }

    @Test
    void execute_treatsZeroRetentionAsUnlimited() {

        when(settings.getInt(eq(SettingKeys.BACKUP_RETENTION_COUNT), anyInt())).thenReturn(0);
        when(backupRepository.findCompletedAutomaticBackups()).thenReturn(backups(50));

        service.execute();

        verify(deleteBackupService, never()).deleteArchive(any());
    }

    @Test
    void execute_prunesTheOldestArchives() {

        when(backupRepository.findCompletedAutomaticBackups()).thenReturn(backups(5));

        List<BackupRecord> pruned = new ArrayList<>();

        org.mockito.Mockito.doAnswer(invocation -> {
            pruned.add(invocation.getArgument(0));
            return null;
        }).when(deleteBackupService).deleteArchive(any());

        service.execute();

        // Index 0 is newest, so 3 and 4 are the ones that should go.
        assertEquals(
                List.of("finance-backup-3.zip", "finance-backup-4.zip"),
                pruned.stream().map(BackupRecord::getFileName).toList()
        );
    }

    @Test
    void execute_continuesPruningAfterOneArchiveCannotBeDeleted() {

        when(backupRepository.findCompletedAutomaticBackups()).thenReturn(backups(5));

        doThrow(new BackupFailedException("File is gone"))
                .when(deleteBackupService).deleteArchive(any());

        // One unreachable archive must not fail the backup that just
        // succeeded, nor stop the rest being pruned.
        service.execute();

        verify(deleteBackupService, times(2)).deleteArchive(any());
    }

    @Test
    void execute_returnsTheBackupItTook() {

        assertEquals("finance-backup-0.zip", service.execute().getFileName());
    }
}
