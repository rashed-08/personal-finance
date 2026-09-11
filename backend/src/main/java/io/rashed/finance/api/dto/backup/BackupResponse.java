package io.rashed.finance.api.dto.backup;

import java.time.LocalDateTime;
import java.util.UUID;

import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupOperation;
import io.rashed.finance.common.enums.BackupProvider;
import io.rashed.finance.common.enums.BackupStatus;
import io.rashed.finance.common.enums.BackupType;

/**
 * One entry in the backup/restore audit trail.
 *
 * @param filePath     local path or Drive link. Null for a restore, which
 *                     consumes an archive rather than producing one.
 * @param downloadable whether the archive can still be fetched through
 *                     the API, so the UI knows to offer the button.
 */
public record BackupResponse(
        UUID id,
        BackupOperation operation,
        BackupType backupType,
        BackupProvider provider,
        BackupFormat format,
        String fileName,
        String filePath,
        Long fileSize,
        String checksum,
        BackupStatus status,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        boolean downloadable
) {
}
