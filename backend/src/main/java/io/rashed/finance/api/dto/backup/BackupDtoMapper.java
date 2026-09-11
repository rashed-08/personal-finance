package io.rashed.finance.api.dto.backup;

import io.rashed.finance.domain.backup.BackupRecord;

public final class BackupDtoMapper {

    private BackupDtoMapper() {
    }

    public static BackupResponse toResponse(BackupRecord record) {

        return new BackupResponse(
                record.getId().getValue(),
                record.getOperation(),
                record.getBackupType(),
                record.getProvider(),
                record.getFormat(),
                record.getFileName(),
                record.getFilePath(),
                record.getFileSize(),
                record.getChecksum(),
                record.getStatus(),
                record.getErrorMessage(),
                record.getStartedAt(),
                record.getCompletedAt(),
                isDownloadable(record)
        );
    }

    /**
     * A restore has no archive of its own, a failed or in-progress backup
     * never finished writing one, and a deleted or pruned archive no
     * longer exists.
     */
    private static boolean isDownloadable(BackupRecord record) {

        return record.isBackup() && record.isCompleted() && record.hasStoredArchive();
    }
}
