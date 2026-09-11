package io.rashed.finance.api.dto.backup;

import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupProvider;

/**
 * Asks for a backup now.
 *
 * Both fields are optional overrides; omitting them uses the
 * {@code BACKUP_PROVIDER} and {@code BACKUP_FORMAT} settings, which is
 * the normal case. They exist so a one-off local copy can be taken while
 * Drive is the configured default.
 */
public record CreateBackupRequest(
        BackupProvider provider,
        BackupFormat format
) {
}
