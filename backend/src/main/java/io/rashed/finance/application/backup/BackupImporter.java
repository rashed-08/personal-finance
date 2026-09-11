package io.rashed.finance.application.backup;

import io.rashed.finance.common.enums.BackupFormat;

/**
 * Loads an archive back into the database, replacing what is there.
 *
 * Implementations are destructive by contract: see
 * docs/operations/RestoreStrategy.md. Callers are responsible for
 * verifying the archive checksum and for obtaining explicit confirmation
 * before calling this.
 */
public interface BackupImporter {

    BackupFormat format();

    /**
     * @throws BackupFailedException if the archive is unreadable or the
     *         load fails. Implementations must leave the database in a
     *         consistent state — either fully restored or untouched.
     */
    void restore(BackupArchive archive);
}
