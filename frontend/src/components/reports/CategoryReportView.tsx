import { useState } from "react";

import ChartCard from "../charts/ChartCard";
import TrendLineChart from "../charts/TrendLineChart";
import { useCategories } from "../../hooks/useCategories";
import { useCategoryReport } from "../../hooks/useReports";
import { formatCurrency } from "../../lib/format";

export default function CategoryReportView() {
    const { data: categories = [] } = useCategories();

    const [categoryId, setCategoryId] = useState<string>("");
    const [fromDate, setFromDate] = useState("");
    const [toDate, setToDate] = useState("");

    const filter = categoryId
        ? { categoryId, fromDate: fromDate || undefined, toDate: toDate || undefined }
        : undefined;

    const { data, isLoading, error } = useCategoryReport(filter);

    return (
        <div>
            <div className="filter-bar">
                <div className="field">
                    <label className="field__label" htmlFor="cat-report-category">
                        Category<span className="field__req">*</span>
                    </label>
                    <select
                        id="cat-report-category"
                        className="select"
                        value={categoryId}
                        onChange={(e) => setCategoryId(e.target.value)}
                    >
                        <option value="">Select a category…</option>
                        {categories.map((c) => (
                            <option key={c.id} value={c.id}>{c.name}</option>
                        ))}
                    </select>
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="cat-report-from">From</label>
                    <input
                        id="cat-report-from"
                        type="date"
                        className="input"
                        value={fromDate}
                        onChange={(e) => setFromDate(e.target.value)}
                    />
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="cat-report-to">To</label>
                    <input
                        id="cat-report-to"
                        type="date"
                        className="input"
                        value={toDate}
                        onChange={(e) => setToDate(e.target.value)}
                    />
                </div>
            </div>

            {!categoryId ? (
                <div className="card">
                    <div className="state">
                        <div className="state__desc">Choose a category to see its spending history.</div>
                    </div>
                </div>
            ) : isLoading ? (
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
                            <span className="stat-card__label">Total Spending</span>
                            <span className="stat-card__value">{formatCurrency(data.totalSpending)}</span>
                            <span className="stat-card__meta">{data.transactionCount} transactions</span>
                        </div>
                        <div className="card stat-card">
                            <span className="stat-card__label">Average per Month</span>
                            <span className="stat-card__value">{formatCurrency(data.averagePerMonth)}</span>
                        </div>
                    </div>

                    <ChartCard
                        title="Monthly Trend"
                        isEmpty={data.monthlyTrend.length === 0}
                        emptyMessage="No transactions for this category yet."
                    >
                        <TrendLineChart
                            data={data.monthlyTrend.map((m) => ({ label: m.yearMonth, value: m.total }))}
                            valueFormatter={formatCurrency}
                        />
                    </ChartCard>
                </>
            )}
        </div>
    );
}
