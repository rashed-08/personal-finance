package io.rashed.finance.application.backup;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.rashed.finance.application.settings.SettingKeys;
import io.rashed.finance.application.settings.SettingsProvider;
import io.rashed.finance.common.enums.BackupType;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.domain.backup.BackupRepository;

/**
 * Takes the scheduled backup and prunes old ones.
 *
 * The schedule comes from {@code app.backup.cron} rather than from the
 * {@code AUTO_BACKUP_CRON} setting: Spring reads a cron expression once,
 * at startup, so a schedule stored in the database could not take effect
 * until a restart anyway. The setting is still what decides <em>whether</em>
 * a run does anything, which does apply immediately.
 *
 * A failure is logged and swallowed. The run already recorded itself as
 * FAILED in history, and letting the exception escape a scheduled method
 * achieves nothing except a second stack trace.
 */
@Service
public class RunScheduledBackupService {

    private static final Logger log = LoggerFactory.getLogger(RunScheduledBackupService.class);

    private final CreateBackupService createBackupService;
    private final DeleteBackupService deleteBackupService;
    private final BackupRepository backupRepository;
    private final SettingsProvider settings;

    public RunScheduledBackupService(
            CreateBackupService createBackupService,
            DeleteBackupService deleteBackupService,
            BackupRepository backupRepository,
            SettingsProvider settings
    ) {
        this.createBackupService = Objects.requireNonNull(createBackupService);
        this.deleteBackupService = Objects.requireNonNull(deleteBackupService);
        this.backupRepository = Objects.requireNonNull(backupRepository);
        this.settings = Objects.requireNonNull(settings);
    }

    /**
     * Default 02:00 daily. Set {@code app.backup.cron} to {@code -} to
     * stop Spring scheduling this at all.
     */
    @Scheduled(cron = "${app.backup.cron:0 0 2 * * *}")
    public void runScheduled() {

        if (!settings.getBoolean(SettingKeys.AUTO_BACKUP_ENABLED, false)) {
            return;
        }

        try {
            execute();

        } catch (RuntimeException ex) {
            log.error("Scheduled backup failed: {}", ex.getMessage());
        }
    }

    /**
     * Runs an automatic backup and applies retention. Separate from
     * {@link #runScheduled()} so it can be called directly and so it
     * propagates failures to its caller.
     */
    public BackupRecord execute() {

        BackupRecord record = createBackupService.execute(BackupType.AUTOMATIC);

        pruneOldBackups();

        return record;
    }

    /**
     * Keeps the newest {@code BACKUP_RETENTION_COUNT} automatic backups
     * and deletes the archives behind the rest.
     *
     * Manual backups are never pruned: someone took them on purpose.
     * History entries survive pruning — only the archives go.
     */
    private void pruneOldBackups() {

        int retention = settings.getInt(
                SettingKeys.BACKUP_RETENTION_COUNT,
                SettingKeys.BACKUP_RETENTION_COUNT_FALLBACK
        );

        if (retention <= 0) {
            return;
        }

        List<BackupRecord> automatic = backupRepository.findCompletedAutomaticBackups();

        if (automatic.size() <= retention) {
            return;
        }

        List<BackupRecord> expired = automatic.subList(retention, automatic.size());

        log.info(
                "Retention is {} automatic backups; pruning {} older archive(s).",
                retention, expired.size()
        );

        for (BackupRecord record : expired) {

            try {
                deleteBackupService.deleteArchive(record);

            } catch (RuntimeException ex) {
                // One unreachable archive must not stop the rest being
                // pruned, nor fail the backup that just succeeded.
                log.warn(
                        "Could not prune archive {}: {}",
                        record.getFileName(), ex.getMessage()
                );
            }
        }
    }
}
