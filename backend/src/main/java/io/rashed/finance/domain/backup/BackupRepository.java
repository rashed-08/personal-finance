package io.rashed.finance.domain.backup;

import java.util.List;
import java.util.Optional;

import io.rashed.finance.common.enums.BackupOperation;

public interface BackupRepository {

    BackupRecord save(BackupRecord record);

    Optional<BackupRecord> findById(BackupId id);

    /**
     * Newest first — the history screen and retention pruning both read in
     * that order.
     */
    List<BackupRecord> findAll();

    List<BackupRecord> findByOperation(BackupOperation operation);

    /**
     * Completed automatic backups, newest first. Retention prunes from the
     * tail of this list; manual backups are never pruned automatically.
     */
    List<BackupRecord> findCompletedAutomaticBackups();

    void delete(BackupId id);
}
