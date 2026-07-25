export interface ImportGoogleKeepRequest {
    content: string;
}

export interface GoogleKeepMigrationResult {
    importedCount: number;
    skippedCount: number;
    warnings: string[];
    errors: string[];
    durationMillis: number;
}
