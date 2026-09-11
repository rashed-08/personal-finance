export type BackupOperation = "BACKUP" | "RESTORE";

export type BackupType = "MANUAL" | "AUTOMATIC";

export type BackupProvider = "LOCAL" | "GOOGLE_DRIVE" | "S3";

export type BackupFormat = "JSON" | "PG_DUMP";

export type BackupStatus = "IN_PROGRESS" | "COMPLETED" | "FAILED";

export interface Backup {
    id: string;

    operation: BackupOperation;

    backupType: BackupType;

    provider: BackupProvider;

    format: BackupFormat;

    fileName: string;

    /** Local path or Drive link. Null for a restore, which consumes an archive. */
    filePath: string | null;

    fileSize: number | null;

    checksum: string | null;

    status: BackupStatus;

    errorMessage: string | null;

    startedAt: string;

    completedAt: string | null;

    /** Whether the archive can still be fetched through the API. */
    downloadable: boolean;
}

export interface GoogleDriveConnection {
    /** Whether the server has OAuth credentials. False is not user-fixable. */
    configured: boolean;

    connected: boolean;

    accountEmail: string | null;

    folderName: string | null;

    /** Why Drive is unusable, when it is. */
    detail: string | null;
}

export interface BackupConfiguration {
    provider: BackupProvider;

    format: BackupFormat;

    autoBackupEnabled: boolean;

    retentionCount: number;

    /** Exact text a restore request must echo. Supplied by the server so the UI never hardcodes it. */
    confirmationPhrase: string;

    googleDrive: GoogleDriveConnection;
}

export interface CreateBackupRequest {
    /** Optional one-off overrides; omit to use the configured defaults. */
    provider?: BackupProvider;
    format?: BackupFormat;
}
