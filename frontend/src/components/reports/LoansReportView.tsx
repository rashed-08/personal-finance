import { Fragment, useState } from "react";

import { useLoanReports } from "../../hooks/useReports";
import { formatCurrency } from "../../lib/format";
import LoanTypeBadge from "../loans/LoanTypeBadge";
import type { LoanReportLine } from "../../types/report";

function statusPillClass(status: LoanReportLine["loanStatus"]): string {
    switch (status) {
        case "ACTIVE":
            return "pill pill--active";
        case "CANCELLED":
            return "pill pill--reversed";
        default:
            return "pill pill--inactive";
    }
}

function statusLabel(status: LoanReportLine["loanStatus"]): string {
    switch (status) {
        case "ACTIVE":
            return "Active";
        case "CANCELLED":
            return "Cancelled";
        default:
            return "Closed";
    }
}

export default function LoansReportView() {
    const { data = [], isLoading, error } = useLoanReports(false);
    const [expanded, setExpanded] = useState<string | null>(null);

    if (isLoading) {
        return (
            <div className="card">
                <div className="state">
                    <div className="spinner" />
                    <div className="state__desc">Loading loans…</div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="card">
                <div className="state">
                    <div className="state__icon">⚠</div>
                    <div className="state__title">Couldn't load loan reports</div>
                </div>
            </div>
        );
    }

    if (data.length === 0) {
        return (
            <div className="card">
                <div className="state">
                    <div className="state__desc">No loans yet.</div>
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
                            <th>Name</th>
                            <th>Type</th>
                            <th className="col-right">Principal</th>
                            <th className="col-right">Paid</th>
                            <th className="col-right">Remaining</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {data.map((line) => (
                            <Fragment key={line.loanId}>
                                <tr
                                    onClick={() => setExpanded(expanded === line.loanId ? null : line.loanId)}
                                    style={{ cursor: "pointer" }}
                                >
                                    <td className="cell-name">{line.name}</td>
                                    <td><LoanTypeBadge type={line.loanType} /></td>
                                    <td className="col-right cell-amount">{formatCurrency(line.principalAmount)}</td>
                                    <td className="col-right cell-amount">{formatCurrency(line.paidAmount)}</td>
                                    <td className="col-right cell-amount">{formatCurrency(line.remainingAmount)}</td>
                                    <td>
                                        <span className={statusPillClass(line.loanStatus)}>
                                            <span className="pill__dot" />
                                            {statusLabel(line.loanStatus)}
                                        </span>
                                    </td>
                                </tr>
                                {expanded === line.loanId && (
                                    <tr>
                                        <td colSpan={6} style={{ padding: 0 }}>
                                            <div className="reconciliation-detail">
                                                {line.paymentHistory.length === 0 ? (
                                                    <p className="reconciliation-detail__empty">
                                                        No payments recorded yet.
                                                    </p>
                                                ) : (
                                                    <ul className="reconciliation-detail__snapshots">
                                                        {line.paymentHistory.map((payment) => (
                                                            <li key={payment.transactionId}>
                                                                <span>{payment.date}</span>
                                                                <span>{payment.description || "—"}</span>
                                                                <span style={{ marginLeft: "auto" }}>
                                                                    {formatCurrency(payment.amount)}
                                                                </span>
                                                            </li>
                                                        ))}
                                                    </ul>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                )}
                            </Fragment>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
