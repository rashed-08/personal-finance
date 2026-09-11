package io.rashed.finance.infrastructure.backup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.rashed.finance.application.backup.BackupArchive;
import io.rashed.finance.application.backup.BackupFailedException;
import io.rashed.finance.application.backup.BackupStorage;
import io.rashed.finance.application.settings.SettingKeys;
import io.rashed.finance.application.settings.SettingsProvider;
import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupProvider;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises local storage against a real temporary directory: the point of
 * this class is filesystem behaviour, which a mocked Path would not test.
 */
class LocalBackupStorageTest {

    private static final byte[] CONTENT = "ledger".getBytes(StandardCharsets.UTF_8);

    @TempDir
    Path backupDirectory;

    private LocalBackupStorage storage;

    @BeforeEach
    void setUp() {

        SettingsProvider settings = mock(SettingsProvider.class);

        when(settings.getString(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        when(settings.getString(
                SettingKeys.BACKUP_DIRECTORY,
                SettingKeys.BACKUP_DIRECTORY_FALLBACK
        )).thenReturn(backupDirectory.toString());

        storage = new LocalBackupStorage(settings);
    }

    private static BackupArchive archive(String fileName) {
        return BackupArchive.of(fileName, BackupFormat.JSON, CONTENT);
    }

    private static BackupStorage.StoredArchiveLocation locationOf(
            String fileName,
            BackupStorage.StoredArchive stored
    ) {
        return new BackupStorage.StoredArchiveLocation(
                fileName, stored.path(), stored.storageReference());
    }

    // -------------------------------------------------------------------------
    // Provider contract
    // -------------------------------------------------------------------------

    @Test
    void isAlwaysAvailable() {

        assertEquals(BackupProvider.LOCAL, storage.provider());
        assertTrue(storage.isAvailable());
        assertThrows(IllegalStateException.class, () -> storage.unavailableReason());
    }

    // -------------------------------------------------------------------------
    // Store
    // -------------------------------------------------------------------------

    @Test
    void store_writesTheArchiveToDisk() throws IOException {

        BackupStorage.StoredArchive stored = storage.store(archive("archive.zip"));

        Path written = Path.of(stored.path());

        assertTrue(Files.isRegularFile(written));
        assertArrayEquals(CONTENT, Files.readAllBytes(written));

        // Local storage addresses archives by path, not by an object id.
        assertNull(stored.storageReference());
    }

    @Test
    void store_createsTheDirectoryOnDemand() {

        Path nested = backupDirectory.resolve("does/not/exist/yet");

        SettingsProvider settings = mock(SettingsProvider.class);
        when(settings.getString(anyString(), anyString())).thenReturn(nested.toString());

        BackupStorage.StoredArchive stored =
                new LocalBackupStorage(settings).store(archive("archive.zip"));

        assertTrue(Files.isRegularFile(Path.of(stored.path())));
    }

    // -------------------------------------------------------------------------
    // Retrieve
    // -------------------------------------------------------------------------

    @Test
    void retrieve_readsBackTheSameBytes() {

        BackupStorage.StoredArchive stored = storage.store(archive("archive.zip"));

        BackupArchive read = storage.retrieve(locationOf("archive.zip", stored));

        assertArrayEquals(CONTENT, read.content());
        assertEquals(BackupArchive.sha256(CONTENT), read.checksum());
    }

    @Test
    void retrieve_reportsAMissingFile() {

        BackupFailedException thrown = assertThrows(
                BackupFailedException.class,
                () -> storage.retrieve(new BackupStorage.StoredArchiveLocation(
                        "gone.zip", backupDirectory.resolve("gone.zip").toString(), null))
        );

        assertTrue(thrown.getMessage().contains("no longer exists"));
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Test
    void delete_removesTheFileFromDisk() {

        BackupStorage.StoredArchive stored = storage.store(archive("archive.zip"));

        Path written = Path.of(stored.path());
        assertTrue(Files.exists(written));

        storage.delete(locationOf("archive.zip", stored));

        assertFalse(Files.exists(written));
    }

    @Test
    void delete_isIdempotent() {

        BackupStorage.StoredArchive stored = storage.store(archive("archive.zip"));

        storage.delete(locationOf("archive.zip", stored));

        // Deleting an archive that is already gone succeeds: absence is the
        // requested outcome.
        storage.delete(locationOf("archive.zip", stored));
    }

    @Test
    void delete_resolvesByFileNameWhenNoPathWasRecorded() {

        storage.store(archive("archive.zip"));

        storage.delete(new BackupStorage.StoredArchiveLocation("archive.zip", null, null));

        assertFalse(Files.exists(backupDirectory.resolve("archive.zip")));
    }

    // -------------------------------------------------------------------------
    // Path traversal
    // -------------------------------------------------------------------------

    @Test
    void aFileNameCannotEscapeTheBackupDirectory() {

        // Generated names are safe, but a restore-or-delete-by-name request
        // arrives from the client.
        for (String hostile : new String[]{"../escaped.zip", "../../etc/passwd", "sub/../../out.zip"}) {

            assertThrows(
                    BackupFailedException.class,
                    () -> storage.delete(
                            new BackupStorage.StoredArchiveLocation(hostile, null, null)),
                    "file name '" + hostile + "' must be refused"
            );
        }
    }
}
