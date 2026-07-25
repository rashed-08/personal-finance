import { useState } from "react";

import { useAccounts } from "../../hooks/useAccounts";
import { useCategories } from "../../hooks/useCategories";
import { useTransactions } from "../../hooks/useTransactions";
import type { Transaction, TransactionFilter } from "../../types/transaction";

import TransactionDialog from "../../components/transactions/TransactionDialog";
import TransactionFilters from "../../components/transactions/TransactionFilters";
import TransactionForm from "../../components/transactions/TransactionForm";
import TransactionTable from "../../components/transactions/TransactionTable";

const PAGE_SIZE = 20;

export default function TransactionsPage() {
    const [filter, setFilter] = useState<TransactionFilter>({});
    const [page, setPage] = useState(0);

    const { data: accounts = [] } = useAccounts();
    const { data: categories = [] } = useCategories();

    const { data, isLoading, error } = useTransactions({
        ...filter,
        page,
        size: PAGE_SIZE,
    });

    const [open, setOpen] = useState(false);
    const [selectedTransaction, setSelectedTransaction] = useState<
        Transaction | undefined
    >();

    function createNew() {
        setSelectedTransaction(undefined);
        setOpen(true);
    }

    function editTransaction(transaction: Transaction) {
        setSelectedTransaction(transaction);
        setOpen(true);
    }

    function closeDialog() {
        setOpen(false);
        setSelectedTransaction(undefined);
    }

    function changeFilter(next: TransactionFilter) {
        setFilter(next);
        setPage(0);
    }

    const transactions = data?.content ?? [];
    const totalPages = data?.totalPages ?? 0;
    const totalElements = data?.totalElements ?? 0;

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Transactions</h1>
                    <p className="page-header__subtitle">
                        Every income, expense, transfer and correction. History is
                        never rewritten — corrections create new transactions.
                    </p>
                </div>

                <button type="button" className="btn btn--primary" onClick={createNew}>
                    + New Transaction
                </button>
            </div>

            <TransactionFilters filter={filter} onChange={changeFilter} />

            <div className="card">
                {isLoading ? (
                    <div className="state">
                        <div className="spinner" />
                        <div className="state__desc">Loading transactions…</div>
                    </div>
                ) : error ? (
                    <div className="state">
                        <div className="state__icon">⚠</div>
                        <div className="state__title">Couldn’t load transactions</div>
                        <div className="state__desc">
                            Check that the backend is running on
                            <code> localhost:8080</code> and try again.
                        </div>
                    </div>
                ) : transactions.length === 0 ? (
                    <div className="state">
                        <div className="state__icon">🧾</div>
                        <div className="state__title">No transactions yet</div>
                        <div className="state__desc">
                            Record your first income, expense or transfer to start
                            building your ledger.
                        </div>
                        <button
                            type="button"
                            className="btn btn--primary"
                            onClick={createNew}
                        >
                            + New Transaction
                        </button>
                    </div>
                ) : (
                    <>
                        <TransactionTable
                            transactions={transactions}
                            accounts={accounts}
                            categories={categories}
                            onEdit={editTransaction}
                        />

                        {totalPages > 1 && (
                            <div className="pagination">
                                <span>
                                    {totalElements} transaction
                                    {totalElements === 1 ? "" : "s"} · page{" "}
                                    {page + 1} of {totalPages}
                                </span>

                                <div className="pagination__controls">
                                    <button
                                        type="button"
                                        className="btn btn--ghost btn--sm"
                                        disabled={page === 0}
                                        onClick={() => setPage((p) => p - 1)}
                                    >
                                        Previous
                                    </button>
                                    <button
                                        type="button"
                                        className="btn btn--ghost btn--sm"
                                        disabled={page + 1 >= totalPages}
                                        onClick={() => setPage((p) => p + 1)}
                                    >
                                        Next
                                    </button>
                                </div>
                            </div>
                        )}
                    </>
                )}
            </div>

            <TransactionDialog
                open={open}
                title={selectedTransaction ? "Edit Transaction" : "New Transaction"}
                onClose={closeDialog}
            >
                <TransactionForm
                    transaction={selectedTransaction}
                    onSuccess={closeDialog}
                />
            </TransactionDialog>
        </>
    );
}
