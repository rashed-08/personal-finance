package io.rashed.finance.domain.backup;

import org.junit.jupiter.api.Test;

import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupOperation;
import io.rashed.finance.common.enums.BackupProvider;
import io.rashed.finance.common.enums.BackupStatus;
import io.rashed.finance.common.enums.BackupType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupRecordTest {

    private static BackupRecord startedBackup() {

        return BackupRecord.startBackup(
                BackupType.MANUAL,
                BackupProvider.LOCAL,
                BackupFormat.JSON,
                "finance-backup-2026-09-11T143207.zip"
        );
    }

    // -------------------------------------------------------------------------
    // Opening a record
    // -------------------------------------------------------------------------

    @Test
    void startBackup_opensAnInProgressRecord() {

        BackupRecord record = startedBackup();

        assertTrue(record.isBackup());
        assertTrue(record.isInProgress());
        assertEquals(BackupStatus.IN_PROGRESS, record.getStatus());
        assertNotNull(record.getStartedAt());

        // Nothing is known about the archive until the work is done.
        assertEquals(null, record.getCompletedAt());
        assertEquals(null, record.getFileSize());
        assertFalse(record.hasChecksum());
    }

    @Test
    void startRestore_isAlwaysManual() {

        // Nothing schedules a restore.
        BackupRecord record = BackupRecord.startRestore(
                BackupProvider.GOOGLE_DRIVE, BackupFormat.JSON, "archive.zip");

        assertTrue(record.isRestore());
        assertEquals(BackupOperation.RESTORE, record.getOperation());
        assertEquals(BackupType.MANUAL, record.getBackupType());
        assertFalse(record.isAutomatic());
    }

    @Test
    void startBackup_requiresAFileName() {

        assertThrows(
                NullPointerException.class,
                () -> BackupRecord.startBackup(
                        BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.JSON, null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> BackupRecord.startBackup(
                        BackupType.MANUAL, BackupProvider.LOCAL, BackupFormat.JSON, " ")
        );
    }

    // -------------------------------------------------------------------------
    // Closing a record
    // -------------------------------------------------------------------------

    @Test
    void complete_recordsWhereTheArchiveWentAndHowBigItIs() {

        BackupRecord completed = startedBackup()
                .complete("/backups/archive.zip", null, 2048L, "abc123");

        assertTrue(completed.isCompleted());
        assertEquals("/backups/archive.zip", completed.getFilePath());
        assertEquals(2048L, completed.getFileSize());
        assertEquals("abc123", completed.getChecksum());
        assertTrue(completed.hasChecksum());
        assertNotNull(completed.getCompletedAt());
        assertEquals(null, completed.getErrorMessage());
    }

    @Test
    void fail_keepsTheReason() {

        BackupRecord failed = startedBackup().fail("Disk full");

        assertTrue(failed.isFailed());
        assertEquals("Disk full", failed.getErrorMessage());
        assertNotNull(failed.getCompletedAt());
    }

    @Test
    void fail_truncatesAnOverlongProviderMessage() {

        // Provider errors can be whole HTML pages; the column holds 2000.
        BackupRecord failed = startedBackup().fail("x".repeat(5000));

        assertEquals(2000, failed.getErrorMessage().length());
    }

    @Test
    void aClosedRecordCannotBeClosedAgain() {

        // History is append-only: one outcome per operation.
        BackupRecord completed = startedBackup().complete("/p", null, 1L, "c");

        assertThrows(IllegalStateException.class, () -> completed.fail("later failure"));
        assertThrows(IllegalStateException.class, () -> completed.complete("/p", null, 1L, "c"));

        BackupRecord failed = startedBackup().fail("nope");

        assertThrows(IllegalStateException.class, () -> failed.complete("/p", null, 1L, "c"));
    }

    // -------------------------------------------------------------------------
    // Invariants
    // -------------------------------------------------------------------------

    @Test
    void constructor_rejectsNegativeFileSize() {

        BackupRecord record = startedBackup();

        assertThrows(
                IllegalArgumentException.class,
                () -> record.complete("/p", null, -1L, "c")
        );
    }

    @Test
    void constructor_rejectsCompletionBeforeStart() {

        BackupRecord started = startedBackup();

        assertThrows(
                IllegalArgumentException.class,
                () -> new BackupRecord(
                        started.getId(),
                        BackupOperation.BACKUP,
                        BackupType.MANUAL,
                        BackupProvider.LOCAL,
                        BackupFormat.JSON,
                        "archive.zip",
                        null,
                        null,
                        null,
                        null,
                        BackupStatus.COMPLETED,
                        null,
                        started.getStartedAt(),
                        started.getStartedAt().minusMinutes(1),
                        started.getCreatedAt(),
                        started.getUpdatedAt()
                )
        );
    }

    @Test
    void isStoredLocally_distinguishesTheProvider() {

        assertTrue(startedBackup().isStoredLocally());

        assertFalse(
                BackupRecord.startBackup(
                        BackupType.MANUAL,
                        BackupProvider.GOOGLE_DRIVE,
                        BackupFormat.JSON,
                        "archive.zip"
                ).isStoredLocally()
        );
    }

    // -------------------------------------------------------------------------
    // Archive lifetime
    // -------------------------------------------------------------------------

    @Test
    void aCompletedBackupHasAStoredArchive() {

        assertTrue(startedBackup().complete("/backups/a.zip", null, 10L, "c").hasStoredArchive());
    }

    @Test
    void aDriveBackupIsLocatedByItsStorageReference() {

        assertTrue(
                startedBackup()
                        .complete("https://drive.google.com/file/d/abc", "abc", 10L, "c")
                        .hasStoredArchive()
        );
    }

    @Test
    void anOpenOrFailedRecordHasNoStoredArchive() {

        assertFalse(startedBackup().hasStoredArchive());
        assertFalse(startedBackup().fail("Disk full").hasStoredArchive());
    }

    @Test
    void aRestoreHasNoStoredArchive() {

        // It consumes an archive rather than producing one.
        assertFalse(
                BackupRecord.startRestore(BackupProvider.LOCAL, BackupFormat.JSON, "a.zip")
                        .complete(null, null, 10L, "c")
                        .hasStoredArchive()
        );
    }

    @Test
    void archiveRemoved_clearsTheLocationButKeepsTheEntry() {

        BackupRecord completed = startedBackup()
                .complete("/backups/a.zip", "drive-id", 2048L, "checksum");

        BackupRecord removed = completed.archiveRemoved();

        assertFalse(removed.hasStoredArchive());
        assertEquals(null, removed.getFilePath());
        assertEquals(null, removed.getStorageReference());

        // Everything that makes the entry an audit record survives, so a
        // deleted backup stays distinguishable from one that never ran.
        assertEquals(completed.getId(), removed.getId());
        assertEquals(completed.getFileName(), removed.getFileName());
        assertEquals(2048L, removed.getFileSize());
        assertEquals("checksum", removed.getChecksum());
        assertEquals(BackupStatus.COMPLETED, removed.getStatus());
        assertEquals(completed.getStartedAt(), removed.getStartedAt());
        assertEquals(completed.getCompletedAt(), removed.getCompletedAt());
    }

    @Test
    void archiveRemoved_isIdempotent() {

        BackupRecord removed = startedBackup()
                .complete("/backups/a.zip", null, 10L, "c")
                .archiveRemoved();

        assertSame(removed, removed.archiveRemoved());
    }

    @Test
    void archiveRemoved_isNotBlockedByTheClosedRecordGuard() {

        // Unlike complete() and fail() this is not a second outcome for the
        // operation, it is the later fate of its artefact.
        BackupRecord completed = startedBackup().complete("/backups/a.zip", null, 10L, "c");

        assertFalse(completed.archiveRemoved().hasStoredArchive());
    }
}
