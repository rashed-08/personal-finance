package io.rashed.finance.application.backup;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.domain.backup.BackupId;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.domain.backup.BackupRepository;

/**
 * Fetches a stored archive so it can be downloaded.
 *
 * Keeping a copy off the machine that made it is the point of a backup,
 * so this exists even when the provider is LOCAL.
 */
@Service
public class DownloadBackupService {

    private static final Logger log = LoggerFactory.getLogger(DownloadBackupService.class);

    private final BackupRepository backupRepository;
    private final BackupStorages storages;

    public DownloadBackupService(BackupRepository backupRepository, BackupStorages storages) {
        this.backupRepository = Objects.requireNonNull(backupRepository);
        this.storages = Objects.requireNonNull(storages);
    }

    public BackupArchive execute(BackupId id) {

        BackupRecord record = backupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Backup not found."));

        if (!record.isBackup() || !record.isCompleted()) {
            throw new TransactionValidationException(
                    "Only a completed backup has an archive to download.");
        }

        if (!record.hasStoredArchive()) {
            throw new TransactionValidationException(
                    "The archive for " + record.getFileName()
                            + " has been deleted. The history entry is kept for the record, "
                            + "but there is no file left to download.");
        }

        BackupArchive archive = storages.requireAvailable(record.getProvider())
                .retrieve(new BackupStorage.StoredArchiveLocation(
                        record.getFileName(),
                        record.getFilePath(),
                        record.getStorageReference()
                ));

        // Worth knowing the archive on disk no longer matches what was
        // written, even though the download itself is allowed to proceed:
        // the user may be recovering precisely because something is wrong.
        if (!archive.matches(record.getChecksum())) {
            log.warn(
                    "Archive {} no longer matches its recorded checksum; it has been modified "
                            + "or corrupted since it was created.",
                    record.getFileName()
            );
        }

        return archive;
    }
}
