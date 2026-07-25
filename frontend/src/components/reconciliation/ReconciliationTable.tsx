import { Fragment, useState } from "react";

import type { Account } from "../../types/account";
import type { CashReconciliation } from "../../types/cashReconciliation";
import { formatCurrency } from "../../lib/format";
import ReconciliationDetail from "./ReconciliationDetail";

interface Props {
    reconciliations: CashReconciliation[];
    accounts: Account[];
}

export default function ReconciliationTable({ reconciliations, accounts }: Props) {
    const [expandedId, setExpandedId] = useState<string | null>(null);

    function accountName(accountId: string): string {
        return accounts.find((a) => a.id === accountId)?.name ?? "Unknown account";
    }

    return (
        <div className="table-wrap">
            <table className="table">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Account</th>
                        <th className="col-right">Expected</th>
                        <th className="col-right">Actual</th>
                        <th className="col-right">Difference</th>
                        <th>Status</th>
                        <th className="col-right">Actions</th>
                    </tr>
                </thead>

                <tbody>
                    {reconciliations.map((reconciliation) => {
                        const difference = reconciliation.differenceAmount;
                        const isExpanded = expandedId === reconciliation.id;

                        return (
                            <Fragment key={reconciliation.id}>
                                <tr>
                                    <td>{reconciliation.reconciliationDate}</td>
                                    <td>{accountName(reconciliation.accountId)}</td>
                                    <td className="col-right cell-amount">
                                        {formatCurrency(reconciliation.expectedCashAmount)}
                                    </td>
                                    <td className="col-right cell-amount">
                                        {reconciliation.actualCashAmount === null
                                            ? "—"
                                            : formatCurrency(reconciliation.actualCashAmount)}
                                    </td>
                                    <td
                                        className={
                                            difference === null
                                                ? "col-right cell-amount"
                                                : difference > 0
                                                  ? "col-right cell-amount cell-amount--increase"
                                                  : difference < 0
                                                    ? "col-right cell-amount cell-amount--decrease"
                                                    : "col-right cell-amount"
                                        }
                                    >
                                        {difference === null ? "—" : formatCurrency(difference)}
                                    </td>
                                    <td>
                                        <span
                                            className={
                                                reconciliation.status === "COMPLETED"
                                                    ? "pill pill--active"
                                                    : "pill pill--inactive"
                                            }
                                        >
                                            <span className="pill__dot" />
                                            {reconciliation.status === "COMPLETED" ? "Completed" : "Pending"}
                                        </span>
                                    </td>
                                    <td>
                                        <div className="row-actions">
                                            <button
                                                type="button"
                                                className="btn btn--ghost btn--sm"
                                                onClick={() =>
                                                    setExpandedId(isExpanded ? null : reconciliation.id)
                                                }
                                            >
                                                {isExpanded ? "Hide" : "Manage"}
                                            </button>
                                        </div>
                                    </td>
                                </tr>

                                {isExpanded && (
                                    <tr>
                                        <td colSpan={7} style={{ padding: 0 }}>
                                            <ReconciliationDetail reconciliation={reconciliation} />
                                        </td>
                                    </tr>
                                )}
                            </Fragment>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
}
