package io.rashed.finance.application.backup;

import io.rashed.finance.common.enums.BackupFormat;

/**
 * Turns the current database contents into an archive.
 *
 * One implementation per {@link BackupFormat}; the format to use comes
 * from the {@code BACKUP_FORMAT} setting and is resolved by
 * {@link BackupFormats}.
 */
public interface BackupExporter {

    BackupFormat format();

    /**
     * @param fileName name to record on the archive, extension included.
     * @throws BackupFailedException if the archive cannot be produced.
     */
    BackupArchive export(String fileName);
}
