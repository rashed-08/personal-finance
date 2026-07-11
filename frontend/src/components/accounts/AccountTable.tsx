import { useState } from "react";

import type { Account } from "../../types/account";

import { useDeleteAccount, useDeactivateAccount } from "../../hooks/useAccounts";
import { formatCurrency } from "../../lib/format";
import AccountTypeBadge from "./AccountTypeBadge";

interface Props {
    accounts: Account[];
    onEdit(account: Account): void;
}

export default function AccountTable({ accounts, onEdit }: Props) {
    const deactivate = useDeactivateAccount();
    const remove = useDeleteAccount();

    // Delete is destructive; require an inline confirmation first.
    const [confirmingId, setConfirmingId] = useState<string | null>(null);

    return (
        <div className="table-wrap">
            <table className="table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Type</th>
                        <th className="col-right">Opening Balance</th>
                        <th>Status</th>
                        <th className="col-right">Actions</th>
                    </tr>
                </thead>

                <tbody>
                    {accounts.map((account) => {
                        const isConfirming = confirmingId === account.id;
                        const isRemoving =
                            remove.isPending && remove.variables === account.id;

                        return (
                            <tr key={account.id}>
                                <td>
                                    <div className="cell-name">{account.name}</div>
                                    {account.description && (
                                        <div className="cell-desc">
                                            {account.description}
                                        </div>
                                    )}
                                </td>

                                <td>
                                    <AccountTypeBadge type={account.accountType} />
                                </td>

                                <td className="col-right cell-amount">
                                    {formatCurrency(account.openingBalance)}
                                </td>

                                <td>
                                    <span
                                        className={
                                            account.active
                                                ? "pill pill--active"
                                                : "pill pill--inactive"
                                        }
                                    >
                                        <span className="pill__dot" />
                                        {account.active ? "Active" : "Inactive"}
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
                                                    remove.mutate(account.id, {
                                                        onSettled: () =>
                                                            setConfirmingId(null),
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
                                                onClick={() => onEdit(account)}
                                            >
                                                Edit
                                            </button>

                                            <button
                                                type="button"
                                                className="btn btn--ghost btn--sm"
                                                disabled={
                                                    !account.active ||
                                                    deactivate.isPending
                                                }
                                                onClick={() =>
                                                    deactivate.mutate(account.id)
                                                }
                                            >
                                                Deactivate
                                            </button>

                                            <button
                                                type="button"
                                                className="btn btn--danger btn--sm"
                                                onClick={() =>
                                                    setConfirmingId(account.id)
                                                }
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
