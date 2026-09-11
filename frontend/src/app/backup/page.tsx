import { useState } from "react";

import { useBackupConfiguration, useBackupHistory } from "../../hooks/useBackups";
import type { Backup } from "../../types/backup";

import BackupHistoryTable from "../../components/backup/BackupHistoryTable";
import GoogleDriveCard from "../../components/backup/GoogleDriveCard";
import RestoreDialog from "../../components/backup/RestoreDialog";
import RestoreUploadPanel from "../../components/backup/RestoreUploadPanel";
import RunBackupPanel from "../../components/backup/RunBackupPanel";

export default function BackupPage() {
    const configuration = useBackupConfiguration();
    const history = useBackupHistory();

    // Exactly one of these is set while the confirmation dialog is open.
    const [restoring, setRestoring] = useState<Backup | null>(null);
    const [uploaded, setUploaded] = useState<File | null>(null);

    function closeRestore() {
        setRestoring(null);
        setUploaded(null);
    }

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Backup &amp; Restore</h1>
                    <p className="page-header__subtitle">
                        Take a copy of your financial data, keep it somewhere safe, and
                        load it back if you ever need to. Every backup and restore is
                        recorded below, successes and failures alike.
                    </p>
                </div>
            </div>

            {configuration.isLoading ? (
                <div className="card">
                    <div className="state">
                        <div className="spinner" />
                        <div className="state__desc">Loading backup configuration…</div>
                    </div>
                </div>
            ) : configuration.error || !configuration.data ? (
                <div className="card">
                    <div className="state">
                        <div className="state__icon">⚠</div>
                        <div className="state__title">Couldn’t load backup settings</div>
                        <div className="state__desc">
                            Check that the backend is running on
                            <code> localhost:8080</code> and try again.
                        </div>
                    </div>
                </div>
            ) : (
                <>
                    <div className="dashboard-section">
                        <h2 className="dashboard-section__title">Back up now</h2>
                        <RunBackupPanel configuration={configuration.data} />
                    </div>

                    <div className="dashboard-section">
                        <h2 className="dashboard-section__title">Cloud storage</h2>
                        <GoogleDriveCard connection={configuration.data.googleDrive} />
                    </div>

                    <div className="dashboard-section">
                        <h2 className="dashboard-section__title">Restore</h2>
                        <RestoreUploadPanel onSelected={setUploaded} />
                    </div>

                    <div className="dashboard-section">
                        <h2 className="dashboard-section__title">History</h2>

                        <div className="card">
                            {history.isLoading ? (
                                <div className="state">
                                    <div className="spinner" />
                                    <div className="state__desc">Loading history…</div>
                                </div>
                            ) : history.error ? (
                                <div className="state">
                                    <div className="state__icon">⚠</div>
                                    <div className="state__title">
                                        Couldn’t load history
                                    </div>
                                </div>
                            ) : (history.data ?? []).length === 0 ? (
                                <div className="state">
                                    <div className="state__icon">💾</div>
                                    <div className="state__title">No backups yet</div>
                                    <div className="state__desc">
                                        Take your first backup above. Nothing leaves this
                                        machine unless you choose a cloud destination.
                                    </div>
                                </div>
                            ) : (
                                <BackupHistoryTable
                                    backups={history.data ?? []}
                                    onRestore={setRestoring}
                                />
                            )}
                        </div>
                    </div>

                    <RestoreDialog
                        backup={restoring}
                        file={uploaded}
                        confirmationPhrase={configuration.data.confirmationPhrase}
                        onClose={closeRestore}
                        onRestored={closeRestore}
                    />
                </>
            )}
        </>
    );
}
