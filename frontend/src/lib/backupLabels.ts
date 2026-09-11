import type {
    BackupFormat,
    BackupProvider,
    BackupStatus,
} from "../types/backup";

export const PROVIDER_LABELS: Record<BackupProvider, string> = {
    LOCAL: "This machine",
    GOOGLE_DRIVE: "Google Drive",
    S3: "S3",
};

export const FORMAT_LABELS: Record<BackupFormat, string> = {
    JSON: "JSON export",
    PG_DUMP: "PostgreSQL dump",
};

export const STATUS_LABELS: Record<BackupStatus, string> = {
    IN_PROGRESS: "In progress",
    COMPLETED: "Completed",
    FAILED: "Failed",
};

/**
 * Reuses the transaction-status pill styles: the three states map onto the
 * same green / grey / red meanings, so the screen stays visually
 * consistent without new CSS.
 */
export function statusPillClass(status: BackupStatus): string {
    switch (status) {
        case "COMPLETED":
            return "pill pill--active";
        case "FAILED":
            return "pill pill--void";
        case "IN_PROGRESS":
            return "pill pill--reversed";
    }
}

/** Byte count as a short human-readable size, e.g. 2.4 MB. */
export function formatFileSize(bytes: number | null): string {
    if (bytes == null) {
        return "—";
    }

    if (bytes < 1024) {
        return `${bytes} B`;
    }

    const units = ["KB", "MB", "GB"];
    let size = bytes / 1024;
    let unit = 0;

    while (size >= 1024 && unit < units.length - 1) {
        size /= 1024;
        unit += 1;
    }

    return `${size.toFixed(1)} ${units[unit]}`;
}

/** ISO timestamp as a compact local date and time. */
export function formatTimestamp(value: string | null): string {
    if (!value) {
        return "—";
    }

    const parsed = new Date(value);

    if (Number.isNaN(parsed.getTime())) {
        return value;
    }

    return parsed.toLocaleString(undefined, {
        year: "numeric",
        month: "short",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    });
}
