package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.domain.backup.BackupId;
import io.rashed.finance.domain.backup.BackupRecord;
import io.rashed.finance.infrastructure.persistence.entity.BackupHistoryEntity;

public final class BackupHistoryEntityMapper {

    private BackupHistoryEntityMapper() {
    }

    public static BackupHistoryEntity toEntity(BackupRecord record) {

        if (record == null) {
            return null;
        }

        return new BackupHistoryEntity(
                record.getId().getValue(),
                record.getOperation(),
                record.getBackupType(),
                record.getProvider(),
                record.getFormat(),
                record.getFileName(),
                record.getFilePath(),
                record.getStorageReference(),
                record.getFileSize(),
                record.getStatus(),
                record.getChecksum(),
                record.getErrorMessage(),
                record.getStartedAt(),
                record.getCompletedAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    public static BackupRecord toDomain(BackupHistoryEntity entity) {

        if (entity == null) {
            return null;
        }

        return new BackupRecord(
                BackupId.of(entity.getId()),
                entity.getOperation(),
                entity.getBackupType(),
                entity.getProvider(),
                entity.getFormat(),
                entity.getFileName(),
                entity.getFilePath(),
                entity.getStorageReference(),
                entity.getFileSize(),
                entity.getChecksum(),
                entity.getStatus(),
                entity.getErrorMessage(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
