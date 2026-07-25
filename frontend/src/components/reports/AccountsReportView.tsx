import { useState } from "react";

import { useAccountBalances, useAccountStatement } from "../../hooks/useReports";
import { formatCurrency } from "../../lib/format";

export default function AccountsReportView() {
    const { data: balances = [], isLoading, error } = useAccountBalances();

    const [accountId, setAccountId] = useState<string>("");
    const [fromDate, setFromDate] = useState("");
    const [toDate, setToDate] = useState("");

    const statement = useAccountStatement(accountId || undefined, fromDate || undefined, toDate || undefined);

    return (
        <div>
            <div className="dashboard-section">
                <h2 className="dashboard-section__title">Account Balances</h2>
                <div className="card">
                    {isLoading ? (
                        <div className="state">
                            <div className="spinner" />
                            <div className="state__desc">Loading balances…</div>
                        </div>
                    ) : error ? (
                        <div className="state">
                            <div className="state__icon">⚠</div>
                            <div className="state__title">Couldn't load account balances</div>
                        </div>
                    ) : (
                        <div className="table-wrap">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>Account</th>
                                        <th className="col-right">Balance</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {balances.map((b) => (
                                        <tr key={b.accountId}>
                                            <td className="cell-name">{b.accountName}</td>
                                            <td className="col-right cell-amount">{formatCurrency(b.balance)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>

            <div className="dashboard-section">
                <h2 className="dashboard-section__title">Account Statement</h2>

                <div className="filter-bar">
                    <div className="field">
                        <label className="field__label" htmlFor="statement-account">
                            Account<span className="field__req">*</span>
                        </label>
                        <select
                            id="statement-account"
                            className="select"
                            value={accountId}
                            onChange={(e) => setAccountId(e.target.value)}
                        >
                            <option value="">Select an account…</option>
                            {balances.map((b) => (
                                <option key={b.accountId} value={b.accountId}>{b.accountName}</option>
                            ))}
                        </select>
                    </div>

                    <div className="field">
                        <label className="field__label" htmlFor="statement-from">From</label>
                        <input
                            id="statement-from"
                            type="date"
                            className="input"
                            value={fromDate}
                            onChange={(e) => setFromDate(e.target.value)}
                        />
                    </div>

                    <div className="field">
                        <label className="field__label" htmlFor="statement-to">To</label>
                        <input
                            id="statement-to"
                            type="date"
                            className="input"
                            value={toDate}
                            onChange={(e) => setToDate(e.target.value)}
                        />
                    </div>
                </div>

                {!accountId ? (
                    <div className="card">
                        <div className="state">
                            <div className="state__desc">Choose an account to see its transaction history.</div>
                        </div>
                    </div>
                ) : statement.isLoading ? (
                    <div className="card">
                        <div className="state">
                            <div className="spinner" />
                            <div className="state__desc">Loading statement…</div>
                        </div>
                    </div>
                ) : statement.error || !statement.data ? (
                    <div className="card">
                        <div className="state">
                            <div className="state__icon">⚠</div>
                            <div className="state__title">Couldn't load this statement</div>
                        </div>
                    </div>
                ) : (
                    <div className="card">
                        <div className="table-wrap">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Description</th>
                                        <th className="col-right">Amount</th>
                                        <th className="col-right">Balance</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td colSpan={3} className="cell-name">Opening Balance</td>
                                        <td className="col-right cell-amount">
                                            {formatCurrency(statement.data.openingBalance)}
                                        </td>
                                    </tr>
                                    {statement.data.lines.map((line) => (
                                        <tr key={line.transactionId}>
                                            <td>{line.transactionDate}</td>
                                            <td>{line.description || "—"}</td>
                                            <td
                                                className={
                                                    line.signedAmount >= 0
                                                        ? "col-right cell-amount cell-amount--increase"
                                                        : "col-right cell-amount cell-amount--decrease"
                                                }
                                            >
                                                {line.signedAmount >= 0 ? "+" : "−"}
                                                {formatCurrency(Math.abs(line.signedAmount))}
                                            </td>
                                            <td className="col-right cell-amount">
                                                {formatCurrency(line.runningBalance)}
                                            </td>
                                        </tr>
                                    ))}
                                    <tr>
                                        <td colSpan={3} className="cell-name">Ending Balance</td>
                                        <td className="col-right cell-amount">
                                            {formatCurrency(statement.data.endingBalance)}
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
