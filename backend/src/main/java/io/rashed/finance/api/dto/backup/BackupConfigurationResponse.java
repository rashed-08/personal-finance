package io.rashed.finance.api.dto.backup;

import io.rashed.finance.application.backup.GoogleDriveConnection;
import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.BackupProvider;

/**
 * What the backup screen needs to render itself in one request.
 *
 * @param provider           configured destination.
 * @param format             configured archive format.
 * @param autoBackupEnabled  whether the schedule is armed.
 * @param retentionCount     automatic backups kept before pruning.
 * @param confirmationPhrase exact text a restore request must echo, so
 *                           the UI can prompt for it without hardcoding
 *                           the server's phrase.
 * @param googleDrive        state of the Drive link.
 */
public record BackupConfigurationResponse(
        BackupProvider provider,
        BackupFormat format,
        boolean autoBackupEnabled,
        int retentionCount,
        String confirmationPhrase,
        GoogleDriveConnection googleDrive
) {
}
