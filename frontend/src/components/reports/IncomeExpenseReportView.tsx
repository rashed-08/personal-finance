import { useState } from "react";

import ChartCard from "../charts/ChartCard";
import CategoryBarChart from "../charts/CategoryBarChart";
import TrendLineChart from "../charts/TrendLineChart";
import { useAccounts } from "../../hooks/useAccounts";
import { useCategories } from "../../hooks/useCategories";
import { useIncomeOrExpenseReport } from "../../hooks/useReports";
import { formatCurrency } from "../../lib/format";
import type { IncomeExpenseReportFilter } from "../../types/report";

export default function IncomeExpenseReportView() {
    const [type, setType] = useState<"INCOME" | "EXPENSE">("EXPENSE");
    const [filter, setFilter] = useState<IncomeExpenseReportFilter>({});

    const { data: accounts = [] } = useAccounts();
    const { data: categories = [] } = useCategories();

    const { data, isLoading, error } = useIncomeOrExpenseReport(type, filter);

    function set(key: keyof IncomeExpenseReportFilter, value: string) {
        setFilter({ ...filter, [key]: value || undefined });
    }

    return (
        <div>
            <div className="filter-bar">
                <div className="field">
                    <span className="field__label">Type</span>
                    <div className="tabs" style={{ border: "none", margin: 0 }}>
                        <button
                            type="button"
                            className={type === "EXPENSE" ? "tab tab--active" : "tab"}
                            onClick={() => setType("EXPENSE")}
                        >
                            Expense
                        </button>
                        <button
                            type="button"
                            className={type === "INCOME" ? "tab tab--active" : "tab"}
                            onClick={() => setType("INCOME")}
                        >
                            Income
                        </button>
                    </div>
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="ie-from">From</label>
                    <input
                        id="ie-from"
                        type="date"
                        className="input"
                        value={filter.fromDate ?? ""}
                        onChange={(e) => set("fromDate", e.target.value)}
                    />
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="ie-to">To</label>
                    <input
                        id="ie-to"
                        type="date"
                        className="input"
                        value={filter.toDate ?? ""}
                        onChange={(e) => set("toDate", e.target.value)}
                    />
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="ie-account">Account</label>
                    <select
                        id="ie-account"
                        className="select"
                        value={filter.accountId ?? ""}
                        onChange={(e) => set("accountId", e.target.value)}
                    >
                        <option value="">All accounts</option>
                        {accounts.map((a) => (
                            <option key={a.id} value={a.id}>{a.name}</option>
                        ))}
                    </select>
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="ie-category">Category</label>
                    <select
                        id="ie-category"
                        className="select"
                        value={filter.categoryId ?? ""}
                        onChange={(e) => set("categoryId", e.target.value)}
                    >
                        <option value="">All categories</option>
                        {categories.map((c) => (
                            <option key={c.id} value={c.id}>{c.name}</option>
                        ))}
                    </select>
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
                <>
                    <div className="stat-grid">
                        <div className="card stat-card">
                            <span className="stat-card__label">
                                Total {type === "INCOME" ? "Income" : "Expense"}
                            </span>
                            <span className="stat-card__value">{formatCurrency(data.total)}</span>
                            <span className="stat-card__meta">{data.transactionCount} transactions</span>
                        </div>
                    </div>

                    <div className="chart-grid">
                        <ChartCard
                            title="By Category"
                            isEmpty={data.byCategory.length === 0}
                            emptyMessage="No transactions in this range."
                        >
                            <CategoryBarChart
                                data={data.byCategory.map((c) => ({ name: c.categoryName, value: c.total }))}
                                valueFormatter={formatCurrency}
                            />
                        </ChartCard>

                        <ChartCard
                            title="By Date"
                            isEmpty={data.byDate.length === 0}
                            emptyMessage="No transactions in this range."
                        >
                            <TrendLineChart
                                data={data.byDate.map((d) => ({ label: d.date, value: d.total }))}
                                valueFormatter={formatCurrency}
                            />
                        </ChartCard>
                    </div>
                </>
            )}
        </div>
    );
}
