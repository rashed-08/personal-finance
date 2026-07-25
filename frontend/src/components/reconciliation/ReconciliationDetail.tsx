import { useState } from "react";
import type { FormEvent } from "react";

import {
    useCompleteReconciliation,
    useRecordCashSnapshot,
} from "../../hooks/useCashReconciliation";
import { formatCurrency } from "../../lib/format";
import type { CashReconciliation } from "../../types/cashReconciliation";

interface Props {
    reconciliation: CashReconciliation;
}

function errorMessage(err: unknown): string {
    const detail = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail;
    return detail ?? "Something went wrong. Please try again.";
}

export default function ReconciliationDetail({ reconciliation }: Props) {
    const recordSnapshot = useRecordCashSnapshot();
    const complete = useCompleteReconciliation();

    const [cashAmount, setCashAmount] = useState("");
    const [snapshotNotes, setSnapshotNotes] = useState("");

    const isPending = reconciliation.status === "PENDING";

    function submitSnapshot(e: FormEvent) {
        e.preventDefault();

        const amount = Number(cashAmount);
        if (!cashAmount || amount < 0) {
            return;
        }

        recordSnapshot.mutate(
            { id: reconciliation.id, cashAmount: amount, notes: snapshotNotes.trim() || undefined },
            {
                onSuccess: () => {
                    setCashAmount("");
                    setSnapshotNotes("");
                },
            },
        );
    }

    return (
        <div className="reconciliation-detail">
            {reconciliation.snapshots.length === 0 ? (
                <p className="reconciliation-detail__empty">No cash counts recorded yet.</p>
            ) : (
                <ul className="reconciliation-detail__snapshots">
                    {reconciliation.snapshots.map((snapshot) => (
                        <li key={snapshot.id}>
                            <span className="mono">
                                {new Date(snapshot.snapshotTime).toLocaleString()}
                            </span>
                            <strong>{formatCurrency(snapshot.cashAmount)}</strong>
                            {snapshot.notes && <span className="cell-desc">{snapshot.notes}</span>}
                        </li>
                    ))}
                </ul>
            )}

            {isPending && (
                <form className="inline-panel" onSubmit={submitSnapshot}>
                    {recordSnapshot.isError && (
                        <div className="form-error" role="alert">
                            <span>⚠</span>
                            <span>{errorMessage(recordSnapshot.error)}</span>
                        </div>
                    )}

                    <div className="inline-panel__row">
                        <input
                            type="number"
                            min="0"
                            step="0.01"
                            className="input"
                            placeholder="Counted cash amount"
                            value={cashAmount}
                            onChange={(e) => setCashAmount(e.target.value)}
                        />
                        <input
                            className="input"
                            placeholder="Notes (optional)"
                            value={snapshotNotes}
                            onChange={(e) => setSnapshotNotes(e.target.value)}
                        />
                    </div>

                    <div className="inline-panel__actions">
                        <button
                            type="submit"
                            className="btn btn--primary btn--sm"
                            disabled={!cashAmount || recordSnapshot.isPending}
                        >
                            {recordSnapshot.isPending ? "Recording…" : "Record Cash Count"}
                        </button>

                        {reconciliation.snapshots.length > 0 && (
                            <button
                                type="button"
                                className="btn btn--ghost btn--sm"
                                disabled={complete.isPending}
                                onClick={() => complete.mutate(reconciliation.id)}
                            >
                                {complete.isPending ? "Completing…" : "Complete Reconciliation"}
                            </button>
                        )}
                    </div>

                    {complete.isError && (
                        <div className="form-error" role="alert">
                            <span>⚠</span>
                            <span>{errorMessage(complete.error)}</span>
                        </div>
                    )}
                </form>
            )}

            {!isPending && (
                <p className="reconciliation-detail__result">
                    {reconciliation.adjustmentTransactionId
                        ? "An adjustment transaction was recorded for the difference above."
                        : "Actual cash matched expected cash exactly — no adjustment was needed."}
                </p>
            )}
        </div>
    );
}
