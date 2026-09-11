package io.rashed.finance.application.backup;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupProvider;
import io.rashed.finance.common.enums.BackupType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.backup.BackupId;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.domain.backup.BackupRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteBackupServiceTest {

    private BackupRepository backupRepository;
    private BackupStorage storage;
    private DeleteBackupService service;

    @BeforeEach
    void setUp() {

        backupRepository = mock(BackupRepository.class);
        storage = mock(BackupStorage.class);

        when(storage.provider()).thenReturn(BackupProvider.LOCAL);
        when(storage.isAvailable()).thenReturn(true);
        when(backupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service = new DeleteBackupService(
                backupRepository, new BackupStorages(List.of(storage)));
    }

    private static BackupRecord completedBackup() {

        return BackupRecord
                .startBackup(
                        BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.JSON, "archive.zip")
                .complete("/backups/archive.zip", null, 2048L, "checksum");
    }

    private void given(BackupRecord record) {
        when(backupRepository.findById(any())).thenReturn(Optional.of(record));
    }

    // -------------------------------------------------------------------------
    // The deletion has to be observable
    // -------------------------------------------------------------------------

    @Test
    void execute_deletesTheArchiveAndRecordsThatItIsGone() {

        given(completedBackup());

        service.execute(BackupId.newId());

        verify(storage).delete(any());

        // Deleting the file is not enough: without saving the record the
        // entry keeps reporting a path and keeps offering download and
        // restore for a file that no longer exists.
        ArgumentCaptor<BackupRecord> saved = ArgumentCaptor.forClass(BackupRecord.class);
        verify(backupRepository).save(saved.capture());

        BackupRecord updated = saved.getValue();

        assertFalse(updated.hasStoredArchive());
        assertNull(updated.getFilePath());
        assertNull(updated.getStorageReference());
    }

    @Test
    void execute_keepsTheHistoryEntryItself() {

        BackupRecord original = completedBackup();
        given(original);

        service.execute(BackupId.newId());

        ArgumentCaptor<BackupRecord> saved = ArgumentCaptor.forClass(BackupRecord.class);
        verify(backupRepository).save(saved.capture());

        BackupRecord updated = saved.getValue();

        // Still distinguishable from a backup that never happened.
        assertEquals(original.getId(), updated.getId());
        assertEquals("archive.zip", updated.getFileName());
        assertEquals(2048L, updated.getFileSize());
        assertEquals("checksum", updated.getChecksum());
        assertTrue(updated.isCompleted());
        assertEquals(original.getStartedAt(), updated.getStartedAt());

        // The row is never removed.
        verify(backupRepository, never()).delete(any());
    }

    @Test
    void execute_isIdempotent() {

        given(completedBackup().archiveRemoved());

        service.execute(BackupId.newId());

        // Nothing left to delete, and nothing to re-record.
        verify(storage, never()).delete(any());
        verify(backupRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Nothing to delete
    // -------------------------------------------------------------------------

    @Test
    void execute_rejectsAnUnknownBackup() {

        when(backupRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.execute(BackupId.newId())
        );
    }

    @Test
    void execute_ignoresAFailedBackup() {

        given(BackupRecord
                .startBackup(
                        BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.JSON, "archive.zip")
                .fail("Disk full"));

        service.execute(BackupId.newId());

        // No archive was ever written.
        verify(storage, never()).delete(any());
    }

    @Test
    void execute_ignoresARestoreEntry() {

        given(BackupRecord
                .startRestore(BackupProvider.LOCAL, BackupFormat.JSON, "archive.zip")
                .complete(null, null, 2048L, "checksum"));

        service.execute(BackupId.newId());

        verify(storage, never()).delete(any());
    }

    @Test
    void execute_leavesTheRecordAloneWhenStorageDeletionFails() {

        given(completedBackup());

        org.mockito.Mockito.doThrow(new BackupFailedException("Permission denied"))
                .when(storage).delete(any());

        assertThrows(
                BackupFailedException.class,
                () -> service.execute(BackupId.newId())
        );

        // The archive is still there, so the record must not claim otherwise.
        verify(backupRepository, never()).save(any());
    }
}
