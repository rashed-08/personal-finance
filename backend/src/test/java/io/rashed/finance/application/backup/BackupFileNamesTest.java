package io.rashed.finance.application.backup;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import io.rashed.finance.common.enums.BackupFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BackupFileNamesTest {

    private static final LocalDateTime AT = LocalDateTime.of(2026, 9, 11, 14, 32, 7);

    @Test
    void forBackup_carriesTheTimestampAndFormat() {

        assertEquals(
                "finance-backup-2026-09-11T143207.zip",
                BackupFileNames.forBackup(BackupFormat.JSON, AT)
        );

        assertEquals(
                "finance-backup-2026-09-11T143207.dump",
                BackupFileNames.forBackup(BackupFormat.PG_DUMP, AT)
        );
    }

    @Test
    void forBackup_producesAWindowsSafeName() {

        // Colons are not legal in Windows file names, and this project is
        // developed on Windows.
        assertFalse(BackupFileNames.forBackup(BackupFormat.JSON, AT).contains(":"));
    }

    @Test
    void formatOf_readsTheExtension() {

        assertEquals(BackupFormat.PG_DUMP, BackupFileNames.formatOf("anything.dump"));
        assertEquals(BackupFormat.PG_DUMP, BackupFileNames.formatOf("ANYTHING.DUMP"));
        assertEquals(BackupFormat.JSON, BackupFileNames.formatOf("anything.zip"));
    }

    @Test
    void formatOf_defaultsToJsonForAnUnknownName() {

        // JSON's importer reports a clear error if the guess was wrong,
        // which beats refusing to look at the file.
        assertEquals(BackupFormat.JSON, BackupFileNames.formatOf("archive"));
        assertEquals(BackupFormat.JSON, BackupFileNames.formatOf(null));
    }
}
