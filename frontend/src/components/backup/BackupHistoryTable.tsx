import { useState } from "react";

import { useDeleteBackup } from "../../hooks/useBackups";
import {
    FORMAT_LABELS,
    PROVIDER_LABELS,
    STATUS_LABELS,
    formatFileSize,
    formatTimestamp,
    statusPillClass,
} from "../../lib/backupLabels";
import { downloadBackup } from "../../services/backup.service";
import type { Backup } from "../../types/backup";

interface Props {
    backups: Backup[];
    onRestore(backup: Backup): void;
}

export default function BackupHistoryTable({ backups, onRestore }: Props) {
    const remove = useDeleteBackup();

    const [downloading, setDownloading] = useState<string | null>(null);

    async function handleDownload(backup: Backup) {
        setDownloading(backup.id);

        try {
            await downloadBackup(backup.id, backup.fileName);
        } finally {
            setDownloading(null);
        }
    }

    return (
        <div className="table-wrap">
            <table className="table">
                <thead>
                    <tr>
                        <th>When</th>
                        <th>Operation</th>
                        <th>Destination</th>
                        <th>Format</th>
                        <th className="col-right">Size</th>
                        <th>Status</th>
                        <th className="col-right">Actions</th>
                    </tr>
                </thead>

                <tbody>
                    {backups.map((backup) => (
                        <tr key={backup.id}>
                            <td>
                                <div className="cell-name">
                                    {formatTimestamp(backup.startedAt)}
                                </div>
                                <div className="cell-desc">{backup.fileName}</div>
                            </td>

                            <td>
                                {backup.operation === "RESTORE" ? "Restore" : "Backup"}
                                <div className="cell-desc">
                                    {backup.backupType === "AUTOMATIC"
                                        ? "Scheduled"
                                        : "Manual"}
                                </div>
                            </td>

                            <td>{PROVIDER_LABELS[backup.provider]}</td>

                            <td>{FORMAT_LABELS[backup.format]}</td>

                            <td className="col-right">
                                {formatFileSize(backup.fileSize)}
                            </td>

                            <td>
                                <span className={statusPillClass(backup.status)}>
                                    <span className="pill__dot" />
                                    {STATUS_LABELS[backup.status]}
                                </span>

                                {backup.errorMessage && (
                                    <div className="cell-desc" title={backup.errorMessage}>
                                        {backup.errorMessage}
                                    </div>
                                )}

                                {/* A completed backup with no archive left:
                                    deleted by hand or pruned by retention.
                                    Said explicitly, or the row looks
                                    identical to one still holding a file. */}
                                {backup.operation === "BACKUP" &&
                                    backup.status === "COMPLETED" &&
                                    !backup.downloadable && (
                                        <div className="cell-desc">Archive deleted</div>
                                    )}
                            </td>

                            <td className="col-right">
                                <div className="row-actions">
                                    {backup.downloadable && (
                                        <>
                                            <button
                                                type="button"
                                                className="btn btn--ghost btn--sm"
                                                disabled={downloading === backup.id}
                                                onClick={() => handleDownload(backup)}
                                            >
                                                {downloading === backup.id
                                                    ? "Downloading…"
                                                    : "Download"}
                                            </button>

                                            <button
                                                type="button"
                                                className="btn btn--ghost btn--sm"
                                                onClick={() => onRestore(backup)}
                                            >
                                                Restore
                                            </button>

                                            <button
                                                type="button"
                                                className="btn btn--danger btn--sm"
                                                disabled={
                                                    remove.isPending &&
                                                    remove.variables === backup.id
                                                }
                                                onClick={() => remove.mutate(backup.id)}
                                                title="Deletes the archive. The history entry is kept."
                                            >
                                                {remove.isPending &&
                                                remove.variables === backup.id
                                                    ? "Deleting…"
                                                    : "Delete"}
                                            </button>
                                        </>
                                    )}
                                </div>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}
