import type { Fund } from "../../types/fund";

import { useActivateFund, useDeactivateFund } from "../../hooks/useFunds";
import { formatCurrency } from "../../lib/format";
import FundTypeBadge from "./FundTypeBadge";

interface Props {
    funds: Fund[];
    onEdit(fund: Fund): void;
}

function GoalProgress({ fund }: { fund: Fund }) {
    if (fund.targetAmount == null || fund.targetAmount <= 0) {
        return <span className="cell-desc">No goal set</span>;
    }

    const percent = Math.min(100, Math.round((fund.balance / fund.targetAmount) * 100));

    return (
        <div className="goal-progress">
            <div className="goal-progress__track">
                <div
                    className="goal-progress__fill"
                    style={{ width: `${Math.max(0, percent)}%` }}
                />
            </div>
            <span className="goal-progress__label">
                {percent}% of {formatCurrency(fund.targetAmount)}
                {fund.targetDate ? ` · by ${fund.targetDate}` : ""}
            </span>
        </div>
    );
}

export default function FundTable({ funds, onEdit }: Props) {
    const activate = useActivateFund();
    const deactivate = useDeactivateFund();

    return (
        <div className="table-wrap">
            <table className="table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Type</th>
                        <th className="col-right">Balance</th>
                        <th>Goal Progress</th>
                        <th>Status</th>
                        <th className="col-right">Actions</th>
                    </tr>
                </thead>

                <tbody>
                    {funds.map((fund) => {
                        const isActivating =
                            activate.isPending && activate.variables === fund.id;
                        const isDeactivating =
                            deactivate.isPending && deactivate.variables === fund.id;

                        return (
                            <tr key={fund.id}>
                                <td>
                                    <div className="cell-name">{fund.name}</div>
                                    {fund.description && (
                                        <div className="cell-desc">
                                            {fund.description}
                                        </div>
                                    )}
                                </td>

                                <td>
                                    <FundTypeBadge type={fund.fundType} />
                                </td>

                                <td className="col-right cell-amount">
                                    {formatCurrency(fund.balance)}
                                </td>

                                <td>
                                    <GoalProgress fund={fund} />
                                </td>

                                <td>
                                    <span
                                        className={
                                            fund.active
                                                ? "pill pill--active"
                                                : "pill pill--inactive"
                                        }
                                    >
                                        <span className="pill__dot" />
                                        {fund.active ? "Active" : "Closed"}
                                    </span>
                                </td>

                                <td>
                                    <div className="row-actions">
                                        <button
                                            type="button"
                                            className="btn btn--ghost btn--sm"
                                            onClick={() => onEdit(fund)}
                                        >
                                            Edit
                                        </button>

                                        {fund.active ? (
                                            <button
                                                type="button"
                                                className="btn btn--ghost btn--sm"
                                                disabled={
                                                    fund.balance !== 0 ||
                                                    isDeactivating
                                                }
                                                title={
                                                    fund.balance !== 0
                                                        ? "A fund can only be closed while its balance is zero."
                                                        : undefined
                                                }
                                                onClick={() =>
                                                    deactivate.mutate(fund.id)
                                                }
                                            >
                                                {isDeactivating
                                                    ? "Closing…"
                                                    : "Close"}
                                            </button>
                                        ) : (
                                            <button
                                                type="button"
                                                className="btn btn--ghost btn--sm"
                                                disabled={isActivating}
                                                onClick={() =>
                                                    activate.mutate(fund.id)
                                                }
                                            >
                                                {isActivating
                                                    ? "Reopening…"
                                                    : "Reopen"}
                                            </button>
                                        )}
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
