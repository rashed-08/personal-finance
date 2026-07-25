import { useState } from "react";
import type { FormEvent } from "react";

import { useAccounts } from "../../hooks/useAccounts";
import { useStartReconciliation } from "../../hooks/useCashReconciliation";

interface Props {
    onSuccess: () => void;
}

function today(): string {
    return new Date().toISOString().slice(0, 10);
}

function errorMessage(err: unknown): string {
    const detail = (err as { response?: { data?: { detail?: string } } })
        ?.response?.data?.detail;
    return detail ?? "Something went wrong. Please try again.";
}

export default function StartReconciliationForm({ onSuccess }: Props) {
    const { data: accounts = [] } = useAccounts();
    const cashAccounts = accounts.filter((a) => a.accountType === "CASH" && a.active);

    const startMutation = useStartReconciliation();

    const [accountId, setAccountId] = useState("");
    const [reconciliationDate, setReconciliationDate] = useState(today());
    const [notes, setNotes] = useState("");
    const [formError, setFormError] = useState<string | null>(null);

    function submit(e: FormEvent) {
        e.preventDefault();
        setFormError(null);

        if (!accountId) {
            setFormError("Select which cash account to reconcile.");
            return;
        }

        startMutation.mutate(
            { accountId, reconciliationDate, notes: notes.trim() || undefined },
            { onSuccess },
        );
    }

    return (
        <form className="form" onSubmit={submit} noValidate>
            {formError && (
                <div className="form-error" role="alert">
                    <span>⚠</span>
                    <span>{formError}</span>
                </div>
            )}

            {startMutation.isError && (
                <div className="form-error" role="alert">
                    <span>⚠</span>
                    <span>{errorMessage(startMutation.error)}</span>
                </div>
            )}

            <div className="field">
                <label className="field__label" htmlFor="recon-account">
                    Cash Account<span className="field__req">*</span>
                </label>
                <select
                    id="recon-account"
                    className="select"
                    value={accountId}
                    onChange={(e) => setAccountId(e.target.value)}
                >
                    <option value="">Select a cash account</option>
                    {cashAccounts.map((a) => (
                        <option key={a.id} value={a.id}>
                            {a.name}
                        </option>
                    ))}
                </select>
                {cashAccounts.length === 0 && (
                    <span className="field__hint">
                        No active CASH-type accounts found. Create one on the Accounts page first.
                    </span>
                )}
            </div>

            <div className="field">
                <label className="field__label" htmlFor="recon-date">
                    Reconciliation Date<span className="field__req">*</span>
                </label>
                <input
                    id="recon-date"
                    type="date"
                    className="input"
                    value={reconciliationDate}
                    onChange={(e) => setReconciliationDate(e.target.value)}
                />
                <span className="field__hint">
                    Expected cash is calculated from the ledger as of this date.
                </span>
            </div>

            <div className="field">
                <label className="field__label" htmlFor="recon-notes">
                    Notes
                </label>
                <textarea
                    id="recon-notes"
                    className="textarea"
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    placeholder="Optional context for this session"
                />
            </div>

            <div className="form-actions">
                <button
                    type="submit"
                    className="btn btn--primary"
                    disabled={startMutation.isPending}
                >
                    {startMutation.isPending ? "Starting…" : "Start Reconciliation"}
                </button>
            </div>
        </form>
    );
}
