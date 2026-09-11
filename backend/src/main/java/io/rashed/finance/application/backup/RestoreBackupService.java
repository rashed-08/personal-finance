package io.rashed.finance.application.backup;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupProvider;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.domain.backup.BackupId;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.domain.backup.BackupRepository;

/**
 * Restores the database from an archive. <strong>Destructive.</strong>
 *
 * Three things must hold before any data is touched:
 *
 * <ol>
 *   <li>the caller repeats {@link #CONFIRMATION} exactly — a restore is
 *       never a single mis-click;</li>
 *   <li>the archive's SHA-256 matches the checksum recorded when it was
 *       written, so a corrupted archive is refused rather than loaded;</li>
 *   <li>the archive is readable in its declared format.</li>
 * </ol>
 *
 * Like {@link CreateBackupService} this is not {@code @Transactional}:
 * the history record has to outlive a rollback. The data load itself is
 * atomic — see {@code JsonBackupImporter}.
 */
@Service
public class RestoreBackupService {

    private static final Logger log = LoggerFactory.getLogger(RestoreBackupService.class);

    /**
     * Phrase the client must send to authorize the overwrite. Spelled out
     * rather than a boolean flag so that it cannot be satisfied by a
     * default-constructed request body.
     */
    public static final String CONFIRMATION = "REPLACE ALL DATA";

    private final BackupRepository backupRepository;
    private final BackupFormats formats;
    private final BackupStorages storages;

    public RestoreBackupService(
            BackupRepository backupRepository,
            BackupFormats formats,
            BackupStorages storages
    ) {
        this.backupRepository = Objects.requireNonNull(backupRepository);
        this.formats = Objects.requireNonNull(formats);
        this.storages = Objects.requireNonNull(storages);
    }

    /**
     * Restores from an archive this application stored earlier.
     *
     * @throws ResourceNotFoundException    if the history entry is unknown.
     * @throws TransactionValidationException if the confirmation is wrong,
     *         the entry is not a completed backup, or the archive fails
     *         its checksum.
     */
    public BackupRecord restoreFromHistory(BackupId id, String confirmation) {

        requireConfirmation(confirmation);

        BackupRecord source = backupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Backup not found."));

        if (!source.isBackup() || !source.isCompleted()) {
            throw new TransactionValidationException(
                    "Only a completed backup can be restored; this entry is a "
                            + source.getOperation() + " in state " + source.getStatus() + ".");
        }

        BackupStorage storage = storages.requireAvailable(source.getProvider());

        BackupRecord record = backupRepository.save(
                BackupRecord.startRestore(
                        source.getProvider(),
                        source.getFormat(),
                        source.getFileName()
                )
        );

        return run(record, () -> {

            BackupArchive archive = storage.retrieve(
                    new BackupStorage.StoredArchiveLocation(
                            source.getFileName(),
                            source.getFilePath(),
                            source.getStorageReference()
                    )
            );

            if (!archive.matches(source.getChecksum())) {
                throw new TransactionValidationException(
                        "Archive " + source.getFileName() + " does not match the checksum recorded "
                                + "when it was created. It has been corrupted or modified, and will "
                                + "not be restored.");
            }

            formats.importer(source.getFormat()).restore(archive);

            return archive;
        });
    }

    /**
     * Restores from an archive supplied by the client — a file downloaded
     * from Drive by hand, or one copied off another machine.
     *
     * There is no stored checksum to compare against, so integrity rests
     * on the archive parsing correctly. The provider is recorded as
     * {@link BackupProvider#LOCAL} because that is where the bytes came
     * from as far as this application is concerned.
     */
    public BackupRecord restoreFromUpload(String fileName, byte[] content, String confirmation) {

        requireConfirmation(confirmation);

        Objects.requireNonNull(fileName, "File name cannot be null.");
        Objects.requireNonNull(content, "File content cannot be null.");

        if (content.length == 0) {
            throw new TransactionValidationException("The uploaded archive is empty.");
        }

        BackupFormat format = BackupFileNames.formatOf(fileName);

        BackupRecord record = backupRepository.save(
                BackupRecord.startRestore(BackupProvider.LOCAL, format, fileName)
        );

        return run(record, () -> {

            BackupArchive archive = BackupArchive.of(fileName, format, content);

            formats.importer(format).restore(archive);

            return archive;
        });
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private void requireConfirmation(String confirmation) {

        if (!CONFIRMATION.equals(confirmation)) {
            throw new TransactionValidationException(
                    "A restore replaces all existing financial data. Send confirmation=\""
                            + CONFIRMATION + "\" to proceed.");
        }
    }

    /**
     * Runs the restore, closing out the history record either way.
     */
    private BackupRecord run(BackupRecord record, RestoreStep step) {

        log.warn(
                "Restore started from {} — existing financial data will be replaced.",
                record.getFileName()
        );

        try {
            BackupArchive archive = step.execute();

            BackupRecord completed = backupRepository.save(
                    record.complete(null, null, archive.size(), archive.checksum())
            );

            log.warn("Restore from {} completed.", record.getFileName());

            return completed;

        } catch (RuntimeException ex) {

            try {
                backupRepository.save(record.fail(ex.getMessage()));

            } catch (RuntimeException recordingFailure) {
                log.error("Could not record the restore failure.", recordingFailure);
            }

            log.error("Restore from {} failed: {}", record.getFileName(), ex.getMessage());

            throw ex;
        }
    }

    @FunctionalInterface
    private interface RestoreStep {

        BackupArchive execute();
    }
}
