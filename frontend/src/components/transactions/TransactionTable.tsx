import type { Account } from "../../types/account";
import type { Category } from "../../types/category";
import type { Transaction } from "../../types/transaction";

import { useReverseTransaction, useVoidTransaction } from "../../hooks/useTransactions";
import { formatCurrency } from "../../lib/format";
import TransactionStatusBadge from "./TransactionStatusBadge";
import TransactionTypeBadge from "./TransactionTypeBadge";

interface Props {
    transactions: Transaction[];
    accounts: Account[];
    categories: Category[];
    onEdit(transaction: Transaction): void;
}

/** Whether a transaction increases, decreases, or has no single direction. */
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

export default function TransactionTable({
    transactions,
    accounts,
    categories,
    onEdit,
}: Props) {
    const voidMutation = useVoidTransaction();
    const reverseMutation = useReverseTransaction();

    function accountName(id: string | null): string {
        if (!id) return "—";
        return accounts.find((a) => a.id === id)?.name ?? "Unknown account";
    }

    function categoryName(id: string | null): string {
        if (!id) return "—";
        return categories.find((c) => c.id === id)?.name ?? "Unknown category";
    }

    return (
        <div className="table-wrap">
            <table className="table">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Type</th>
                        <th>Description</th>
                        <th>Account</th>
                        <th>Category</th>
                        <th className="col-right">Amount</th>
                        <th>Status</th>
                        <th className="col-right">Actions</th>
                    </tr>
                </thead>

                <tbody>
                    {transactions.map((txn) => {
                        const direction = amountDirection(txn);
                        const isPosted = txn.transactionStatus === "POSTED";
                        const isVoiding =
                            voidMutation.isPending && voidMutation.variables === txn.id;
                        const isReversing =
                            reverseMutation.isPending &&
                            reverseMutation.variables === txn.id;

                        return (
                            <tr key={txn.id}>
                                <td>{txn.transactionDate}</td>

                                <td>
                                    <TransactionTypeBadge type={txn.transactionType} />
                                </td>

                                <td>
                                    <div className="cell-name">
                                        {txn.description || "—"}
                                    </div>
                                    {txn.notes && (
                                        <div className="cell-desc">{txn.notes}</div>
                                    )}
                                </td>

                                <td>
                                    {txn.transactionType === "TRANSFER"
                                        ? `${accountName(txn.fromAccountId)} → ${accountName(txn.toAccountId)}`
                                        : accountName(txn.fromAccountId ?? txn.toAccountId)}
                                </td>

                                <td>{categoryName(txn.categoryId)}</td>

                                <td
                                    className={
                                        direction === "increase"
                                            ? "col-right cell-amount cell-amount--increase"
                                            : direction === "decrease"
                                              ? "col-right cell-amount cell-amount--decrease"
                                              : "col-right cell-amount"
                                    }
                                >
                                    {direction === "increase" && "+"}
                                    {direction === "decrease" && "−"}
                                    {formatCurrency(txn.amount)}
                                </td>

                                <td>
                                    <TransactionStatusBadge status={txn.transactionStatus} />
                                </td>

                                <td>
                                    <div className="row-actions">
                                        <button
                                            type="button"
                                            className="btn btn--ghost btn--sm"
                                            disabled={!isPosted}
                                            onClick={() => onEdit(txn)}
                                        >
                                            Edit
                                        </button>

                                        <button
                                            type="button"
                                            className="btn btn--ghost btn--sm"
                                            disabled={!isPosted || isVoiding}
                                            onClick={() => voidMutation.mutate(txn.id)}
                                        >
                                            {isVoiding ? "Voiding…" : "Void"}
                                        </button>

                                        <button
                                            type="button"
                                            className="btn btn--danger btn--sm"
                                            disabled={!isPosted || isReversing}
                                            onClick={() => reverseMutation.mutate(txn.id)}
                                        >
                                            {isReversing ? "Reversing…" : "Reverse"}
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
}
