import { useState } from "react";

import type { RecurringTransaction } from "../../types/recurringTransaction";

import {
    useActivateRecurringTransaction,
    useDeactivateRecurringTransaction,
    useDeleteRecurringTransaction,
    useGenerateRecurringTransactionNow,
} from "../../hooks/useRecurringTransactions";
import { formatCurrency } from "../../lib/format";
import { frequencyLabel } from "../../lib/recurringTransactionTypes";
import TransactionTypeBadge from "../transactions/TransactionTypeBadge";

interface Props {
    recurringTransactions: RecurringTransaction[];
    onEdit(recurringTransaction: RecurringTransaction): void;
    onViewHistory(recurringTransaction: RecurringTransaction): void;
}

function isDue(recurringTransaction: RecurringTransaction): boolean {
    return recurringTransaction.nextExecutionDate <= new Date().toISOString().slice(0, 10);
}

export default function RecurringTransactionTable({
    recurringTransactions,
    onEdit,
    onViewHistory,
}: Props) {
    const activate = useActivateRecurringTransaction();
    const deactivate = useDeactivateRecurringTransaction();
    const generateNow = useGenerateRecurringTransactionNow();
    const remove = useDeleteRecurringTransaction();

    const [confirmingId, setConfirmingId] = useState<string | null>(null);

    return (
        <div className="table-wrap">
            <table className="table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Type</th>
                        <th className="col-right">Amount</th>
                        <th>Frequency</th>
                        <th>Next Due</th>
                        <th>Status</th>
                        <th className="col-right">Actions</th>
                    </tr>
                </thead>

                <tbody>
                    {recurringTransactions.map((rt) => {
                        const due = rt.active && isDue(rt);
                        const isConfirming = confirmingId === rt.id;
                        const isRemoving = remove.isPending && remove.variables === rt.id;
                        const isGenerating = generateNow.isPending && generateNow.variables === rt.id;

                        return (
                            <tr key={rt.id}>
                                <td>
                                    <div className="cell-name">{rt.name}</div>
                                    {rt.description && (
                                        <div className="cell-desc">{rt.description}</div>
                                    )}
                                </td>

                                <td>
                                    <TransactionTypeBadge type={rt.transactionType} />
                                </td>

                                <td className="col-right cell-amount">
                                    {formatCurrency(rt.amount)}
                                </td>

                                <td>{frequencyLabel(rt.frequency)}</td>

                                <td>
                                    {rt.nextExecutionDate}
                                    {due && (
                                        <span className="pill pill--reversed" style={{ marginLeft: 6 }}>
                                            <span className="pill__dot" />
                                            Due
                                        </span>
                                    )}
                                </td>

                                <td>
                                    <span className={rt.active ? "pill pill--active" : "pill pill--inactive"}>
                                        <span className="pill__dot" />
                                        {rt.active ? "Active" : "Inactive"}
                                    </span>
                                </td>

                                <td>
                                    {isConfirming ? (
                                        <div className="row-actions confirm">
                                            <span>Delete?</span>
                                            <button
                                                type="button"
                                                className="btn btn--danger btn--sm"
                                                disabled={isRemoving}
                                                onClick={() =>
                                                    remove.mutate(rt.id, {
                                                        onSettled: () => setConfirmingId(null),
                                                    })
                                                }
                                            >
                                                {isRemoving ? "Deleting…" : "Yes"}
                                            </button>
                                            <button
                                                type="button"
                                                className="btn btn--ghost btn--sm"
                                                onClick={() => setConfirmingId(null)}
                                            >
                                                No
                                            </button>
                                        </div>
                                    ) : (
                                        <div className="row-actions">
                                            <button
                                                type="button"
                                                className="btn btn--ghost btn--sm"
                                                onClick={() => onEdit(rt)}
                                            >
                                                Edit
                                            </button>

                                            <button
                                                type="button"
                                                className="btn btn--ghost btn--sm"
                                                onClick={() => onViewHistory(rt)}
                                            >
                                                History
                                            </button>

                                            {!rt.autoGenerate && (
                                                <button
                                                    type="button"
                                                    className="btn btn--ghost btn--sm"
                                                    disabled={!due || isGenerating}
                                                    onClick={() => generateNow.mutate(rt.id)}
                                                >
                                                    {isGenerating ? "Generating…" : "Generate Now"}
                                                </button>
                                            )}

                                            <button
                                                type="button"
                                                className="btn btn--ghost btn--sm"
                                                onClick={() =>
                                                    rt.active
                                                        ? deactivate.mutate(rt.id)
                                                        : activate.mutate(rt.id)
                                                }
                                            >
                                                {rt.active ? "Deactivate" : "Activate"}
                                            </button>

                                            <button
                                                type="button"
                                                className="btn btn--danger btn--sm"
                                                onClick={() => setConfirmingId(rt.id)}
                                            >
                                                Delete
                                            </button>
                                        </div>
                                    )}
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
}
