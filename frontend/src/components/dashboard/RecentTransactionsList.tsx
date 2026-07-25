import { useAccounts } from "../../hooks/useAccounts";
import { useCategories } from "../../hooks/useCategories";
import type { Transaction } from "../../types/transaction";
import { formatCurrency } from "../../lib/format";
import TransactionTypeBadge from "../transactions/TransactionTypeBadge";

interface RecentTransactionsListProps {
    transactions: Transaction[];
}

function amountDirection(txn: Transaction): "increase" | "decrease" | "neutral" {
    switch (txn.transactionType) {
        case "INCOME":
        case "OPENING_BALANCE":
        case "MIGRATION":
            return "increase";
        case "EXPENSE":
            return "decrease";
        case "ADJUSTMENT":
            return txn.toAccountId ? "increase" : "decrease";
        case "TRANSFER":
            return "neutral";
    }
}

export default function RecentTransactionsList({ transactions }: RecentTransactionsListProps) {
    const { data: accounts = [] } = useAccounts();
    const { data: categories = [] } = useCategories();

    function accountName(id: string | null): string {
        if (!id) return "—";
        return accounts.find((a) => a.id === id)?.name ?? "Unknown account";
    }

    function subtitle(txn: Transaction): string {
        const account =
            txn.transactionType === "TRANSFER"
                ? `${accountName(txn.fromAccountId)} → ${accountName(txn.toAccountId)}`
                : accountName(txn.fromAccountId ?? txn.toAccountId);

        const category = txn.categoryId
            ? categories.find((c) => c.id === txn.categoryId)?.name
            : undefined;

        return category ? `${account} · ${category}` : account;
    }

    if (transactions.length === 0) {
        return (
            <div className="state">
                <div className="state__desc">No transactions yet.</div>
            </div>
        );
    }

    return (
        <div>
            {transactions.map((txn) => {
                const direction = amountDirection(txn);

                return (
                    <div key={txn.id} className="list-row">
                        <TransactionTypeBadge type={txn.transactionType} />

                        <div className="list-row__main">
                            <div className="list-row__title">{txn.description || "—"}</div>
                            <div className="list-row__subtitle">
                                {txn.transactionDate} · {subtitle(txn)}
                            </div>
                        </div>

                        <div
                            className={
                                direction === "increase"
                                    ? "cell-amount cell-amount--increase"
                                    : direction === "decrease"
                                      ? "cell-amount cell-amount--decrease"
                                      : "cell-amount"
                            }
                        >
                            {direction === "increase" && "+"}
                            {direction === "decrease" && "−"}
                            {formatCurrency(txn.amount)}
                        </div>
                    </div>
                );
            })}
        </div>
    );
}
