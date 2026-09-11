package io.rashed.finance.application.backup;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupProvider;
import io.rashed.finance.common.enums.BackupType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.domain.backup.BackupId;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.domain.backup.BackupRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestoreBackupServiceTest {

    private static final byte[] CONTENT = "ledger".getBytes(StandardCharsets.UTF_8);

    private static final String CHECKSUM = BackupArchive.sha256(CONTENT);

    private BackupRepository backupRepository;
    private BackupExporter exporter;
    private BackupImporter importer;
    private BackupStorage storage;
    private RestoreBackupService service;

    @BeforeEach
    void setUp() {

        backupRepository = mock(BackupRepository.class);
        exporter = mock(BackupExporter.class);
        importer = mock(BackupImporter.class);
        storage = mock(BackupStorage.class);

        when(exporter.format()).thenReturn(BackupFormat.JSON);
        when(importer.format()).thenReturn(BackupFormat.JSON);
        when(storage.provider()).thenReturn(BackupProvider.LOCAL);
        when(storage.isAvailable()).thenReturn(true);

        when(storage.retrieve(any())).thenReturn(
                BackupArchive.of("archive.zip", BackupFormat.JSON, CONTENT));

        when(backupRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service = new RestoreBackupService(
                backupRepository,
                new BackupFormats(List.of(exporter), List.of(importer)),
                new BackupStorages(List.of(storage))
        );
    }

    /**
     * A completed backup in history, with the checksum of {@link #CONTENT}.
     */
    private BackupRecord completedBackup(String checksum) {

        return BackupRecord
                .startBackup(BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.JSON, "archive.zip")
                .complete("/backups/archive.zip", null, CONTENT.length, checksum);
    }

    private void givenStoredBackup(BackupRecord record) {
        when(backupRepository.findById(any())).thenReturn(Optional.of(record));
    }

    // -------------------------------------------------------------------------
    // Confirmation guard
    // -------------------------------------------------------------------------

    @Test
    void restoreFromHistory_refusesWithoutTheExactConfirmation() {

        givenStoredBackup(completedBackup(CHECKSUM));

        for (String wrong : new String[]{null, "", "yes", "replace all data", "REPLACE ALL DATA "}) {

            assertThrows(
                    TransactionValidationException.class,
                    () -> service.restoreFromHistory(BackupId.newId(), wrong),
                    "confirmation '" + wrong + "' must be refused"
            );
        }

        // Nothing was read, loaded or recorded.
        verify(storage, never()).retrieve(any());
        verify(importer, never()).restore(any());
        verify(backupRepository, never()).save(any());
    }

    @Test
    void restoreFromUpload_refusesWithoutTheExactConfirmation() {

        assertThrows(
                TransactionValidationException.class,
                () -> service.restoreFromUpload("archive.zip", CONTENT, "nope")
        );

        verify(importer, never()).restore(any());
    }

    // -------------------------------------------------------------------------
    // Checksum verification
    // -------------------------------------------------------------------------

    @Test
    void restoreFromHistory_refusesAnArchiveThatFailsItsChecksum() {

        // The archive on disk no longer hashes to what was recorded: it has
        // been corrupted or modified, and must not reach the database.
        givenStoredBackup(completedBackup("0".repeat(64)));

        TransactionValidationException thrown = assertThrows(
                TransactionValidationException.class,
                () -> service.restoreFromHistory(BackupId.newId(), RestoreBackupService.CONFIRMATION)
        );

        assertTrue(thrown.getMessage().contains("checksum"));
        verify(importer, never()).restore(any());
    }

    @Test
    void restoreFromHistory_recordsAFailureWhenTheChecksumIsWrong() {

        givenStoredBackup(completedBackup("0".repeat(64)));

        assertThrows(
                TransactionValidationException.class,
                () -> service.restoreFromHistory(BackupId.newId(), RestoreBackupService.CONFIRMATION)
        );

        ArgumentCaptor<BackupRecord> saved = ArgumentCaptor.forClass(BackupRecord.class);
        verify(backupRepository, org.mockito.Mockito.times(2)).save(saved.capture());

        assertTrue(saved.getAllValues().getLast().isFailed());
    }

    @Test
    void restoreFromHistory_acceptsAnArchiveRecordedWithoutAChecksum() {

        givenStoredBackup(completedBackup(null));

        assertTrue(
                service.restoreFromHistory(BackupId.newId(), RestoreBackupService.CONFIRMATION)
                        .isCompleted()
        );

        verify(importer).restore(any());
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void restoreFromHistory_loadsTheArchiveAndRecordsARestore() {

        givenStoredBackup(completedBackup(CHECKSUM));

        BackupRecord record = service.restoreFromHistory(
                BackupId.newId(), RestoreBackupService.CONFIRMATION);

        assertTrue(record.isRestore());
        assertTrue(record.isCompleted());
        assertEquals("archive.zip", record.getFileName());
        assertEquals(CHECKSUM, record.getChecksum());

        verify(importer).restore(any());
    }

    @Test
    void restoreFromUpload_inferesTheFormatFromTheFileName() {

        BackupRecord record = service.restoreFromUpload(
                "some-archive.zip", CONTENT, RestoreBackupService.CONFIRMATION);

        assertEquals(BackupFormat.JSON, record.getFormat());
        assertTrue(record.isRestore());
        assertTrue(record.isCompleted());

        verify(importer).restore(any());
    }

    @Test
    void restoreFromUpload_rejectsAnEmptyFile() {

        assertThrows(
                TransactionValidationException.class,
                () -> service.restoreFromUpload(
                        "archive.zip", new byte[0], RestoreBackupService.CONFIRMATION)
        );
    }

    // -------------------------------------------------------------------------
    // Source validation
    // -------------------------------------------------------------------------

    @Test
    void restoreFromHistory_rejectsAnUnknownBackup() {

        when(backupRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.restoreFromHistory(BackupId.newId(), RestoreBackupService.CONFIRMATION)
        );
    }

    @Test
    void restoreFromHistory_rejectsAFailedBackup() {

        givenStoredBackup(
                BackupRecord.startBackup(
                                BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.JSON, "archive.zip")
                        .fail("Disk full")
        );

        assertThrows(
                TransactionValidationException.class,
                () -> service.restoreFromHistory(BackupId.newId(), RestoreBackupService.CONFIRMATION)
        );

        verify(importer, never()).restore(any());
    }

    @Test
    void restoreFromHistory_rejectsAnEntryThatIsItselfARestore() {

        givenStoredBackup(
                BackupRecord.startRestore(BackupProvider.LOCAL, BackupFormat.JSON, "archive.zip")
                        .complete(null, null, 10L, CHECKSUM)
        );

        assertThrows(
                TransactionValidationException.class,
                () -> service.restoreFromHistory(BackupId.newId(), RestoreBackupService.CONFIRMATION)
        );
    }

    @Test
    void restoreFromHistory_recordsAFailedImport() {

        givenStoredBackup(completedBackup(CHECKSUM));

        org.mockito.Mockito.doThrow(new BackupFailedException("not a JSON backup"))
                .when(importer).restore(any());

        assertThrows(
                BackupFailedException.class,
                () -> service.restoreFromHistory(BackupId.newId(), RestoreBackupService.CONFIRMATION)
        );

        ArgumentCaptor<BackupRecord> saved = ArgumentCaptor.forClass(BackupRecord.class);
        verify(backupRepository, org.mockito.Mockito.times(2)).save(saved.capture());

        BackupRecord failed = saved.getAllValues().getLast();

        assertTrue(failed.isFailed());
        assertEquals("not a JSON backup", failed.getErrorMessage());
    }

    @Test
    void restoreFromHistory_rejectsABackupWhoseArchiveWasDeleted() {

        givenStoredBackup(completedBackup(CHECKSUM).archiveRemoved());

        TransactionValidationException thrown = assertThrows(
                TransactionValidationException.class,
                () -> service.restoreFromHistory(BackupId.newId(), RestoreBackupService.CONFIRMATION)
        );

        // A clear message, not whatever the storage layer says about a
        // null path.
        assertTrue(thrown.getMessage().contains("deleted"));

        verify(storage, never()).retrieve(any());
        verify(importer, never()).restore(any());
    }
}
