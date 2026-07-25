import { useState } from "react";

import { useCashFlowReport } from "../../hooks/useReports";
import { formatCurrency } from "../../lib/format";

function firstOfMonth(): string {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-01`;
}

function today(): string {
    return new Date().toISOString().slice(0, 10);
}

export default function CashFlowReportView() {
    const [fromDate, setFromDate] = useState(firstOfMonth());
    const [toDate, setToDate] = useState(today());

    const { data, isLoading, error } = useCashFlowReport(fromDate, toDate);

    return (
        <div>
            <div className="filter-bar">
                <div className="field">
                    <label className="field__label" htmlFor="cash-flow-from">From</label>
                    <input
                        id="cash-flow-from"
                        type="date"
                        className="input"
                        value={fromDate}
                        onChange={(e) => setFromDate(e.target.value)}
                    />
                </div>
                <div className="field">
                    <label className="field__label" htmlFor="cash-flow-to">To</label>
                    <input
                        id="cash-flow-to"
                        type="date"
                        className="input"
                        value={toDate}
                        onChange={(e) => setToDate(e.target.value)}
                    />
                </div>
            </div>

            {isLoading ? (
                <div className="card">
                    <div className="state">
                        <div className="spinner" />
                        <div className="state__desc">Loading report…</div>
                    </div>
                </div>
            ) : error || !data ? (
                <div className="card">
                    <div className="state">
                        <div className="state__icon">⚠</div>
                        <div className="state__title">Couldn't load this report</div>
                    </div>
                </div>
            ) : (
                <div className="stat-grid">
                    <div className="card stat-card">
                        <span className="stat-card__label">Money In</span>
                        <span className="stat-card__value stat-card__value--positive">
                            {formatCurrency(data.moneyIn)}
                        </span>
                    </div>
                    <div className="card stat-card">
                        <span className="stat-card__label">Money Out</span>
                        <span className="stat-card__value stat-card__value--negative">
                            {formatCurrency(data.moneyOut)}
                        </span>
                    </div>
                    <div className="card stat-card">
                        <span className="stat-card__label">Net Cash Flow</span>
                        <span
                            className={
                                data.netCashFlow >= 0
                                    ? "stat-card__value stat-card__value--positive"
                                    : "stat-card__value stat-card__value--negative"
                            }
                        >
                            {formatCurrency(data.netCashFlow)}
                        </span>
                    </div>
                    <div className="card stat-card">
                        <span className="stat-card__label">Transfer Volume</span>
                        <span className="stat-card__value">{formatCurrency(data.totalTransferVolume)}</span>
                        <span className="stat-card__meta">Never counted toward net cash flow</span>
                    </div>
                </div>
            )}
        </div>
    );
}
