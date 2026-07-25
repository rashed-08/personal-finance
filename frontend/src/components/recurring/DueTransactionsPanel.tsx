import {
    useDueRecurringTransactions,
    useGenerateRecurringTransactionNow,
    useRunDueRecurringTransactions,
} from "../../hooks/useRecurringTransactions";
import { formatCurrency } from "../../lib/format";
import { frequencyLabel } from "../../lib/recurringTransactionTypes";

function errorMessage(err: unknown): string {
    const detail = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail;
    return detail ?? "Something went wrong. Please try again.";
}

export default function DueTransactionsPanel() {
    const { data = [], isLoading } = useDueRecurringTransactions();
    const runDue = useRunDueRecurringTransactions();
    const generateNow = useGenerateRecurringTransactionNow();

    const autoCount = data.filter((rt) => rt.autoGenerate).length;
    const manualDue = data.filter((rt) => !rt.autoGenerate);

    if (isLoading || data.length === 0) {
        return null;
    }

    return (
        <div className="card" style={{ marginBottom: 20 }}>
            <div className="page-header" style={{ marginBottom: 12 }}>
                <div>
                    <h2 className="page-header__title" style={{ fontSize: 16 }}>
                        Due Now ({data.length})
                    </h2>
                    <p className="page-header__subtitle">
                        {autoCount > 0
                            ? `${autoCount} will generate automatically when you run due transactions.`
                            : "Nothing is set to auto-generate — confirm each one below."}
                    </p>
                </div>

                {autoCount > 0 && (
                    <button
                        type="button"
                        className="btn btn--primary"
                        disabled={runDue.isPending}
                        onClick={() => runDue.mutate()}
                    >
                        {runDue.isPending ? "Running…" : "Run Due Transactions"}
                    </button>
                )}
            </div>

            {runDue.isError && (
                <div className="form-error" role="alert">
                    <span>⚠</span>
                    <span>{errorMessage(runDue.error)}</span>
                </div>
            )}

            {manualDue.length > 0 && (
                <div className="table-wrap">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th className="col-right">Amount</th>
                                <th>Frequency</th>
                                <th>Due Since</th>
                                <th className="col-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {manualDue.map((rt) => {
                                const isGenerating =
                                    generateNow.isPending && generateNow.variables === rt.id;

                                return (
                                    <tr key={rt.id}>
                                        <td>
                                            <div className="cell-name">{rt.name}</div>
                                        </td>
                                        <td className="col-right cell-amount">
                                            {formatCurrency(rt.amount)}
                                        </td>
                                        <td>{frequencyLabel(rt.frequency)}</td>
                                        <td>{rt.nextExecutionDate}</td>
                                        <td>
                                            <div className="row-actions">
                                                <button
                                                    type="button"
                                                    className="btn btn--primary btn--sm"
                                                    disabled={isGenerating}
                                                    onClick={() => generateNow.mutate(rt.id)}
                                                >
                                                    {isGenerating ? "Generating…" : "Confirm"}
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
