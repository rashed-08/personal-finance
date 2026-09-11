package io.rashed.finance.application.backup;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.backup.BackupId;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.domain.backup.BackupRepository;

/**
 * Deletes a stored archive.
 *
 * The history entry stays. History is append-only
 * (docs/database/tables/backup_history.md), so removing the archive is
 * recorded by the entry no longer having a file rather than by the entry
 * disappearing — otherwise a deleted backup would be indistinguishable
 * from one that never happened.
 */
@Service
public class DeleteBackupService {

    private static final Logger log = LoggerFactory.getLogger(DeleteBackupService.class);

    private final BackupRepository backupRepository;
    private final BackupStorages storages;

    public DeleteBackupService(BackupRepository backupRepository, BackupStorages storages) {
        this.backupRepository = Objects.requireNonNull(backupRepository);
        this.storages = Objects.requireNonNull(storages);
    }

    public void execute(BackupId id) {

        BackupRecord record = backupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Backup not found."));

        deleteArchive(record);
    }

    /**
     * Removes the archive behind a record, leaving the record in place.
     * Used by the delete endpoint and by retention pruning.
     */
    void deleteArchive(BackupRecord record) {

        if (!record.isBackup() || !record.isCompleted()) {
            // Nothing was ever stored for a failed or in-progress entry.
            return;
        }

        if (!record.hasStoredArchive()) {
            // Already deleted or pruned. Deleting twice is not an error:
            // the requested outcome is that the archive is gone.
            return;
        }

        storages.get(record.getProvider())
                .delete(new BackupStorage.StoredArchiveLocation(
                        record.getFileName(),
                        record.getFilePath(),
                        record.getStorageReference()
                ));

        // The record has to be updated too, or the deletion is invisible:
        // the entry would keep reporting a file path and keep offering
        // download and restore for an archive that no longer exists.
        backupRepository.save(record.archiveRemoved());

        log.info("Deleted archive for backup {}", record.getFileName());
    }
}
