import { useState } from "react";

import { useRecurringTransactions } from "../../hooks/useRecurringTransactions";
import type { RecurringTransaction } from "../../types/recurringTransaction";

import DueTransactionsPanel from "../../components/recurring/DueTransactionsPanel";
import ExecutionHistoryList from "../../components/recurring/ExecutionHistoryList";
import RecurringTransactionDialog from "../../components/recurring/RecurringTransactionDialog";
import RecurringTransactionForm from "../../components/recurring/RecurringTransactionForm";
import RecurringTransactionTable from "../../components/recurring/RecurringTransactionTable";

export default function RecurringTransactionsPage() {
    const { data = [], isLoading, error } = useRecurringTransactions();

    const [open, setOpen] = useState(false);
    const [selected, setSelected] = useState<RecurringTransaction | undefined>();

    const [historyOpen, setHistoryOpen] = useState(false);
    const [historyTarget, setHistoryTarget] = useState<RecurringTransaction | undefined>();

    function createNew() {
        setSelected(undefined);
        setOpen(true);
    }

    function edit(recurringTransaction: RecurringTransaction) {
        setSelected(recurringTransaction);
        setOpen(true);
    }

    function closeDialog() {
        setOpen(false);
        setSelected(undefined);
    }

    function viewHistory(recurringTransaction: RecurringTransaction) {
        setHistoryTarget(recurringTransaction);
        setHistoryOpen(true);
    }

    function closeHistory() {
        setHistoryOpen(false);
        setHistoryTarget(undefined);
    }

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Recurring Transactions</h1>
                    <p className="page-header__subtitle">
                        Templates that generate transactions on a schedule — rent, bills,
                        subscriptions. Generation is on-demand, not automatic in the
                        background.
                    </p>
                </div>

                <button type="button" className="btn btn--primary" onClick={createNew}>
                    + New Template
                </button>
            </div>

            <DueTransactionsPanel />

            <div className="card">
                {isLoading ? (
                    <div className="state">
                        <div className="spinner" />
                        <div className="state__desc">Loading templates…</div>
                    </div>
                ) : error ? (
                    <div className="state">
                        <div className="state__icon">⚠</div>
                        <div className="state__title">Couldn’t load templates</div>
                        <div className="state__desc">
                            Check that the backend is running on
                            <code> localhost:8080</code> and try again.
                        </div>
                    </div>
                ) : data.length === 0 ? (
                    <div className="state">
                        <div className="state__icon">🔁</div>
                        <div className="state__title">No recurring templates yet</div>
                        <div className="state__desc">
                            Create a template for rent, a bill, or a subscription to
                            automate its entry.
                        </div>
                        <button type="button" className="btn btn--primary" onClick={createNew}>
                            + New Template
                        </button>
                    </div>
                ) : (
                    <RecurringTransactionTable
                        recurringTransactions={data}
                        onEdit={edit}
                        onViewHistory={viewHistory}
                    />
                )}
            </div>

            <RecurringTransactionDialog
                open={open}
                title={selected ? "Edit Template" : "New Template"}
                onClose={closeDialog}
            >
                <RecurringTransactionForm recurringTransaction={selected} onSuccess={closeDialog} />
            </RecurringTransactionDialog>

            <RecurringTransactionDialog
                open={historyOpen}
                title={historyTarget ? `History — ${historyTarget.name}` : "History"}
                onClose={closeHistory}
            >
                {historyTarget && (
                    <ExecutionHistoryList recurringTransactionId={historyTarget.id} />
                )}
            </RecurringTransactionDialog>
        </>
    );
}
