package io.rashed.finance.application.backup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.rashed.finance.common.enums.BackupFormat;

/**
 * Names for backup archives.
 *
 * The name carries the timestamp and the format, which makes a directory
 * listing self-describing and lets {@link #formatOf} pick the right
 * reader for a file uploaded from outside the application.
 */
public final class BackupFileNames {

    private BackupFileNames() {
    }

    private static final String PREFIX = "finance-backup-";

    /**
     * Second precision, no colons: colons are not legal in Windows file
     * names, and two backups in the same second is not a real scenario
     * for a manual action or a nightly schedule.
     */
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss");

    /**
     * e.g. {@code finance-backup-2026-09-11T143207.zip}
     */
    public static String forBackup(BackupFormat format, LocalDateTime at) {

        return PREFIX + at.format(TIMESTAMP) + "." + format.extension();
    }

    /**
     * Infers the format from a file name's extension.
     *
     * Defaults to {@link BackupFormat#JSON} for an unrecognised
     * extension: it is the portable format, and its importer reports a
     * clear "not a JSON backup" error if the guess was wrong — better
     * than refusing to look at the file at all.
     */
    public static BackupFormat formatOf(String fileName) {

        if (fileName != null
                && fileName.toLowerCase().endsWith("." + BackupFormat.PG_DUMP.extension())) {

            return BackupFormat.PG_DUMP;
        }

        return BackupFormat.JSON;
    }
}
