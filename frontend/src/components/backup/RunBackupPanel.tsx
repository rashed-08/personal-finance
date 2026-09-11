import { Link } from "react-router-dom";

import { useCreateBackup } from "../../hooks/useBackups";
import { errorMessage } from "../../lib/errorMessage";
import { FORMAT_LABELS, PROVIDER_LABELS } from "../../lib/backupLabels";
import type { BackupConfiguration } from "../../types/backup";

interface Props {
    configuration: BackupConfiguration;
}

export default function RunBackupPanel({ configuration }: Props) {
    const create = useCreateBackup();

    const driveSelected = configuration.provider === "GOOGLE_DRIVE";
    const driveUnusable = driveSelected && !configuration.googleDrive.connected;

    return (
        <div className="card" style={{ padding: 20 }}>
            <div className="inline-panel">
                <div className="inline-panel__row">
                    <span>Destination</span>
                    <span>{PROVIDER_LABELS[configuration.provider]}</span>
                </div>

                <div className="inline-panel__row">
                    <span>Format</span>
                    <span>{FORMAT_LABELS[configuration.format]}</span>
                </div>

                <div className="inline-panel__row">
                    <span>Automatic backups</span>
                    <span>
                        {configuration.autoBackupEnabled
                            ? `On · keeping the newest ${configuration.retentionCount}`
                            : "Off"}
                    </span>
                </div>
            </div>

            {driveUnusable && (
                <div className="form-warning" role="status" style={{ marginTop: 12 }}>
                    <span>⚠</span>
                    <span>
                        {configuration.googleDrive.detail ??
                            "Google Drive is selected but not connected."}
                    </span>
                </div>
            )}

            {create.isError && (
                <div className="form-error" role="alert" style={{ marginTop: 12 }}>
                    <span>⚠</span>
                    <span>{errorMessage(create.error)}</span>
                </div>
            )}

            {create.isSuccess && (
                <div className="form-warning" role="status" style={{ marginTop: 12 }}>
                    <span>✓</span>
                    <span>
                        Backed up to <code>{create.data.fileName}</code>.
                    </span>
                </div>
            )}

            <div className="form-actions" style={{ marginTop: 16 }}>
                <button
                    type="button"
                    className="btn btn--primary"
                    disabled={create.isPending || driveUnusable}
                    onClick={() => create.mutate({})}
                >
                    {create.isPending ? "Backing up…" : "Back up now"}
                </button>
            </div>

            <p className="field__hint">
                Change the destination, format and retention on the{" "}
                <Link className="auth-link" to="/settings">
                    Settings
                </Link>{" "}
                screen.
            </p>
        </div>
    );
}
