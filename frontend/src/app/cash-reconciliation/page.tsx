import { useState } from "react";

import { useAccounts } from "../../hooks/useAccounts";
import { useReconciliations } from "../../hooks/useCashReconciliation";

import ReconciliationTable from "../../components/reconciliation/ReconciliationTable";
import StartReconciliationDialog from "../../components/reconciliation/StartReconciliationDialog";
import StartReconciliationForm from "../../components/reconciliation/StartReconciliationForm";

export default function CashReconciliationPage() {
    const { data: accounts = [] } = useAccounts();
    const { data: reconciliations = [], isLoading, error } = useReconciliations();

    const [open, setOpen] = useState(false);

    const sorted = [...reconciliations].sort((a, b) =>
        b.reconciliationDate.localeCompare(a.reconciliationDate),
    );

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Cash Reconciliation</h1>
                    <p className="page-header__subtitle">
                        Compare a cash account's ledger-derived balance against what you actually
                        count. Differences become a single adjustment transaction — nothing else
                        is ever rewritten.
                    </p>
                </div>

                <button type="button" className="btn btn--primary" onClick={() => setOpen(true)}>
                    + Start Reconciliation
                </button>
            </div>

            <div className="card">
                {isLoading ? (
                    <div className="state">
                        <div className="spinner" />
                        <div className="state__desc">Loading reconciliation history…</div>
                    </div>
                ) : error ? (
                    <div className="state">
                        <div className="state__icon">⚠</div>
                        <div className="state__title">Couldn’t load reconciliation history</div>
                        <div className="state__desc">
                            Check that the backend is running on
                            <code> localhost:8080</code> and try again.
                        </div>
                    </div>
                ) : sorted.length === 0 ? (
                    <div className="state">
                        <div className="state__icon">💵</div>
                        <div className="state__title">No reconciliations yet</div>
                        <div className="state__desc">
                            Start one for a cash account to compare its ledger balance against a
                            physical count.
                        </div>
                        <button
                            type="button"
                            className="btn btn--primary"
                            onClick={() => setOpen(true)}
                        >
                            + Start Reconciliation
                        </button>
                    </div>
                ) : (
                    <ReconciliationTable reconciliations={sorted} accounts={accounts} />
                )}
            </div>

            <StartReconciliationDialog
                open={open}
                title="Start Reconciliation"
                onClose={() => setOpen(false)}
            >
                <StartReconciliationForm onSuccess={() => setOpen(false)} />
            </StartReconciliationDialog>
        </>
    );
}
