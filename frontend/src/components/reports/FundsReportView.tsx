import { useFundReports } from "../../hooks/useReports";
import { formatCurrency } from "../../lib/format";
import FundTypeBadge from "../funds/FundTypeBadge";

export default function FundsReportView() {
    const { data = [], isLoading, error } = useFundReports(false);

    if (isLoading) {
        return (
            <div className="card">
                <div className="state">
                    <div className="spinner" />
                    <div className="state__desc">Loading funds…</div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="card">
                <div className="state">
                    <div className="state__icon">⚠</div>
                    <div className="state__title">Couldn't load fund reports</div>
                </div>
            </div>
        );
    }

    if (data.length === 0) {
        return (
            <div className="card">
                <div className="state">
                    <div className="state__desc">No funds yet.</div>
                </div>
            </div>
        );
    }

    return (
        <div className="card">
            <div className="table-wrap">
                <table className="table">
                    <thead>
                        <tr>
                            <th>Fund</th>
                            <th className="col-right">Allocated</th>
                            <th className="col-right">Used</th>
                            <th className="col-right">Remaining</th>
                            <th>Progress</th>
                        </tr>
                    </thead>
                    <tbody>
                        {data.map((line) => (
                            <tr key={line.fundId}>
                                <td>
                                    <div className="cell-name">{line.fundName}</div>
                                    <FundTypeBadge type={line.fundType} />
                                </td>
                                <td className="col-right cell-amount">{formatCurrency(line.allocatedAmount)}</td>
                                <td className="col-right cell-amount">{formatCurrency(line.usedAmount)}</td>
                                <td className="col-right cell-amount">{formatCurrency(line.remainingBalance)}</td>
                                <td>
                                    {line.progressPercentage !== null && line.targetAmount !== null ? (
                                        <div className="goal-progress">
                                            <div className="goal-progress__track">
                                                <div
                                                    className="goal-progress__fill"
                                                    style={{ width: `${Math.min(100, Math.max(0, line.progressPercentage))}%` }}
                                                />
                                            </div>
                                            <span className="goal-progress__label">
                                                {line.progressPercentage}% of {formatCurrency(line.targetAmount)}
                                            </span>
                                        </div>
                                    ) : (
                                        <span className="stat-card__meta">No target</span>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
