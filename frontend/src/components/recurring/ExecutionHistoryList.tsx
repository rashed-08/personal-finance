import { useRecurringTransactionExecutions } from "../../hooks/useRecurringTransactions";

interface Props {
    recurringTransactionId: string;
}

export default function ExecutionHistoryList({ recurringTransactionId }: Props) {
    const { data = [], isLoading, error } = useRecurringTransactionExecutions(recurringTransactionId);

    if (isLoading) {
        return (
            <div className="state">
                <div className="spinner" />
                <div className="state__desc">Loading history…</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="state">
                <div className="state__icon">⚠</div>
                <div className="state__desc">Couldn’t load execution history.</div>
            </div>
        );
    }

    if (data.length === 0) {
        return (
            <div className="state">
                <div className="state__icon">🕓</div>
                <div className="state__desc">No occurrences have run yet.</div>
            </div>
        );
    }

    return (
        <div className="table-wrap">
            <table className="table">
                <thead>
                    <tr>
                        <th>Scheduled Date</th>
                        <th>Status</th>
                        <th>Detail</th>
                    </tr>
                </thead>
                <tbody>
                    {data.map((execution) => (
                        <tr key={execution.id}>
                            <td>{execution.scheduledDate}</td>
                            <td>
                                <span
                                    className={
                                        execution.status === "GENERATED"
                                            ? "pill pill--active"
                                            : "pill pill--reversed"
                                    }
                                >
                                    <span className="pill__dot" />
                                    {execution.status === "GENERATED" ? "Generated" : "Skipped"}
                                </span>
                            </td>
                            <td>
                                {execution.status === "SKIPPED" ? (
                                    <span className="cell-desc">{execution.reason}</span>
                                ) : (
                                    "—"
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}
