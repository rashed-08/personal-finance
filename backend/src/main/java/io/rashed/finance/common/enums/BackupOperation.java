package io.rashed.finance.common.enums;

/**
 * Whether a history record describes data leaving the database or
 * returning to it. Both are audited — see
 * docs/database/tables/backup_history.md.
 */
public enum BackupOperation {

    BACKUP,

    RESTORE

}
