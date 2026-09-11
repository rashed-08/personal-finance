import { useEffect, useState } from "react";

import { useRestoreBackup, useRestoreFromUpload } from "../../hooks/useBackups";
import { errorMessage } from "../../lib/errorMessage";
import { FORMAT_LABELS, formatTimestamp } from "../../lib/backupLabels";
import type { Backup } from "../../types/backup";

interface Props {
    /** The archive already in history, or null when restoring an upload. */
    backup: Backup | null;

    /** The uploaded file, or null when restoring from history. */
    file: File | null;

    /** Exact phrase the server requires; supplied by the configuration endpoint. */
    confirmationPhrase: string;

    onClose(): void;
    onRestored(): void;
}

/**
 * The last gate before a restore.
 *
 * The user must type the server's confirmation phrase, which is also what
 * the request carries. That is deliberately more friction than a
 * checkbox: the action replaces every account, transaction, fund and loan
 * in the database, and it cannot be undone from inside the app.
 */
export default function RestoreDialog({
    backup,
    file,
    confirmationPhrase,
    onClose,
    onRestored,
}: Props) {
    const [typed, setTyped] = useState("");

    const fromHistory = useRestoreBackup();
    const fromUpload = useRestoreFromUpload();

    const mutation = backup ? fromHistory : fromUpload;
    const open = backup !== null || file !== null;

    useEffect(() => {
        if (!open) {
            return;
        }

        function onKeyDown(e: KeyboardEvent) {
            if (e.key === "Escape") {
                onClose();
            }
        }

        document.addEventListener("keydown", onKeyDown);
        return () => document.removeEventListener("keydown", onKeyDown);
    }, [open, onClose]);

    if (!open) {
        return null;
    }

    const confirmed = typed.trim() === confirmationPhrase;

    function handleRestore() {
        if (!confirmed) {
            return;
        }

        const onDone = {
            onSuccess: () => {
                setTyped("");
                onRestored();
            },
        };

        if (backup) {
            fromHistory.mutate(
                { id: backup.id, confirmation: confirmationPhrase },
                onDone,
            );
        } else if (file) {
            fromUpload.mutate({ file, confirmation: confirmationPhrase }, onDone);
        }
    }

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div
                className="modal"
                role="dialog"
                aria-modal="true"
                aria-label="Restore backup"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="modal__header">
                    <h2 className="modal__title">Restore backup</h2>
                    <button
                        type="button"
                        className="modal__close"
                        aria-label="Close"
                        onClick={onClose}
                    >
                        ✕
                    </button>
                </div>

                <div className="modal__body">
                    <div className="form-warning" role="alert">
                        <span>⚠</span>
                        <span>
                            This replaces <strong>all</strong> existing accounts,
                            transactions, funds, loans, salary cycles and settings with
                            the contents of this archive. It cannot be undone.
                        </span>
                    </div>

                    <div className="inline-panel">
                        {backup ? (
                            <>
                                <div className="inline-panel__row">
                                    <span>Archive</span>
                                    <span>{backup.fileName}</span>
                                </div>
                                <div className="inline-panel__row">
                                    <span>Created</span>
                                    <span>{formatTimestamp(backup.startedAt)}</span>
                                </div>
                                <div className="inline-panel__row">
                                    <span>Format</span>
                                    <span>{FORMAT_LABELS[backup.format]}</span>
                                </div>
                            </>
                        ) : (
                            <>
                                <div className="inline-panel__row">
                                    <span>Uploaded file</span>
                                    <span>{file?.name}</span>
                                </div>
                                <div className="inline-panel__row">
                                    <span>Checksum</span>
                                    <span>
                                        Not verified — no record of this archive exists
                                    </span>
                                </div>
                            </>
                        )}
                    </div>

                    <div className="field">
                        <label className="field__label" htmlFor="restore-confirmation">
                            Type <code>{confirmationPhrase}</code> to confirm
                            <span className="field__req">*</span>
                        </label>

                        <input
                            id="restore-confirmation"
                            className="input"
                            autoComplete="off"
                            value={typed}
                            onChange={(e) => setTyped(e.target.value)}
                            placeholder={confirmationPhrase}
                        />
                    </div>

                    {mutation.isError && (
                        <div className="form-error" role="alert">
                            <span>⚠</span>
                            <span>{errorMessage(mutation.error)}</span>
                        </div>
                    )}

                    <div className="form-actions">
                        <button
                            type="button"
                            className="btn btn--ghost"
                            onClick={onClose}
                            disabled={mutation.isPending}
                        >
                            Cancel
                        </button>

                        <button
                            type="button"
                            className="btn btn--danger"
                            disabled={!confirmed || mutation.isPending}
                            onClick={handleRestore}
                        >
                            {mutation.isPending ? "Restoring…" : "Restore and replace data"}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
