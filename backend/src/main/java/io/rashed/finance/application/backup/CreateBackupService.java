package io.rashed.finance.application.backup;

import java.time.LocalDateTime;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.rashed.finance.application.settings.SettingKeys;
import io.rashed.finance.application.settings.SettingsProvider;
import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupProvider;
import io.rashed.finance.common.enums.BackupType;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.domain.backup.BackupRepository;

/**
 * Runs a backup: export, store, record.
 *
 * The history record is opened before the work starts and closed after,
 * so a crash leaves evidence rather than silence. That is also why this
 * is deliberately <em>not</em> {@code @Transactional} — a rollback would
 * erase the record of the failure it is rolling back, and the archive
 * itself lives outside the database anyway.
 */
@Service
public class CreateBackupService {

    private static final Logger log = LoggerFactory.getLogger(CreateBackupService.class);

    private final BackupRepository backupRepository;
    private final BackupFormats formats;
    private final BackupStorages storages;
    private final SettingsProvider settings;

    public CreateBackupService(
            BackupRepository backupRepository,
            BackupFormats formats,
            BackupStorages storages,
            SettingsProvider settings
    ) {
        this.backupRepository = Objects.requireNonNull(backupRepository);
        this.formats = Objects.requireNonNull(formats);
        this.storages = Objects.requireNonNull(storages);
        this.settings = Objects.requireNonNull(settings);
    }

    /**
     * Backs up using the configured provider and format.
     */
    public BackupRecord execute(BackupType backupType) {

        return execute(backupType, configuredProvider(), configuredFormat());
    }

    /**
     * @param provider where to store the archive; overrides
     *                 {@code BACKUP_PROVIDER}.
     * @param format   how to serialize it; overrides {@code BACKUP_FORMAT}.
     * @throws BackupFailedException if the provider is unusable, or the
     *         export or upload fails. The failure is recorded in history
     *         first.
     */
    public BackupRecord execute(BackupType backupType, BackupProvider provider, BackupFormat format) {

        Objects.requireNonNull(backupType, "Backup type cannot be null.");
        Objects.requireNonNull(provider, "Backup provider cannot be null.");
        Objects.requireNonNull(format, "Backup format cannot be null.");

        // Checked before any history is written: an unconnected Drive is a
        // configuration problem, not a failed backup attempt.
        BackupStorage storage = storages.requireAvailable(provider);

        String fileName = BackupFileNames.forBackup(format, LocalDateTime.now());

        BackupRecord record = backupRepository.save(
                BackupRecord.startBackup(backupType, provider, format, fileName)
        );

        try {
            BackupArchive archive = formats.exporter(format).export(fileName);

            BackupStorage.StoredArchive stored = storage.store(archive);

            BackupRecord completed = backupRepository.save(
                    record.complete(
                            stored.path(),
                            stored.storageReference(),
                            archive.size(),
                            archive.checksum()
                    )
            );

            log.info(
                    "{} backup {} completed: {} bytes to {}",
                    backupType, fileName, archive.size(), provider
            );

            return completed;

        } catch (RuntimeException ex) {

            // Record the failure, then rethrow. If writing the failure
            // itself fails there is nothing useful left to do, so the
            // original cause is preserved rather than masked.
            try {
                backupRepository.save(record.fail(ex.getMessage()));

            } catch (RuntimeException recordingFailure) {
                log.error("Could not record the backup failure.", recordingFailure);
            }

            log.error("{} backup {} failed: {}", backupType, fileName, ex.getMessage());

            throw ex;
        }
    }

    public BackupProvider configuredProvider() {

        return settings.getEnum(
                SettingKeys.BACKUP_PROVIDER,
                BackupProvider.class,
                BackupProvider.LOCAL
        );
    }

    public BackupFormat configuredFormat() {

        return settings.getEnum(
                SettingKeys.BACKUP_FORMAT,
                BackupFormat.class,
                BackupFormat.JSON
        );
    }
}
