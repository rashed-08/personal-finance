import StatCard from "../dashboard/StatCard";
import type { GoogleKeepMigrationResult } from "../../types/migration";

interface GoogleKeepImportResultPanelProps {
    result: GoogleKeepMigrationResult;
}

export default function GoogleKeepImportResultPanel({ result }: GoogleKeepImportResultPanelProps) {
    return (
        <div className="card" style={{ padding: 20, display: "flex", flexDirection: "column", gap: 16 }}>
            <div className="stat-grid" style={{ marginBottom: 0 }}>
                <StatCard label="Imported" value={String(result.importedCount)} tone="positive" />
                <StatCard label="Skipped (duplicates)" value={String(result.skippedCount)} />
                <StatCard
                    label="Duration"
                    value={`${(result.durationMillis / 1000).toFixed(2)}s`}
                />
            </div>

            {result.errors.length > 0 && (
                <div>
                    <div className="dashboard-section__title" style={{ fontSize: 13, marginBottom: 8 }}>
                        Errors ({result.errors.length})
                    </div>
                    <ul className="message-list">
                        {result.errors.map((error, index) => (
                            <li key={index} className="form-error">
                                <span>⚠</span>
                                <span>{error}</span>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {result.warnings.length > 0 && (
                <div>
                    <div className="dashboard-section__title" style={{ fontSize: 13, marginBottom: 8 }}>
                        Warnings ({result.warnings.length})
                    </div>
                    <ul className="message-list">
                        {result.warnings.map((warning, index) => (
                            <li key={index} className="form-warning">
                                <span>ⓘ</span>
                                <span>{warning}</span>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
}
