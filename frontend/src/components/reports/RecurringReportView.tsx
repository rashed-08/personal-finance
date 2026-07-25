import { useRecurringTransactionReport } from "../../hooks/useReports";

export default function RecurringReportView() {
    const { data = [], isLoading, error } = useRecurringTransactionReport(false);

    if (isLoading) {
        return (
            <div className="card">
                <div className="state">
                    <div className="spinner" />
                    <div className="state__desc">Loading recurring transactions…</div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="card">
                <div className="state">
                    <div className="state__icon">⚠</div>
                    <div className="state__title">Couldn't load this report</div>
                </div>
            </div>
        );
    }

    if (data.length === 0) {
        return (
            <div className="card">
                <div className="state">
                    <div className="state__desc">No recurring transactions yet.</div>
                </div>
            </div>
        );
    }

    return (
        <div className="card">
            <div className="table-wrap">
                <table className="table">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Status</th>
                            <th>Next Due</th>
                            <th>Last Run</th>
                            <th className="col-right">Generated</th>
                            <th className="col-right">Skipped</th>
                        </tr>
                    </thead>
                    <tbody>
                        {data.map((line) => (
                            <tr key={line.recurringTransactionId}>
                                <td className="cell-name">{line.name}</td>
                                <td>
                                    <span className={line.active ? "pill pill--active" : "pill pill--inactive"}>
                                        <span className="pill__dot" />
                                        {line.active ? "Active" : "Inactive"}
                                    </span>
                                </td>
                                <td>{line.nextExecutionDate}</td>
                                <td>{line.lastExecutionDate ?? "—"}</td>
                                <td className="col-right cell-amount">{line.generatedCount}</td>
                                <td className="col-right cell-amount">{line.skippedCount}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
