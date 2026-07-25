import { useState } from "react";

import ChartCard from "../charts/ChartCard";
import CategoryBarChart from "../charts/CategoryBarChart";
import { useMonthlyReport } from "../../hooks/useReports";
import { formatCurrency } from "../../lib/format";

function currentYearMonth(): string {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

function delta(current: number, previous: number): string {
    const diff = current - previous;
    if (diff === 0) return "No change vs. last month";
    const sign = diff > 0 ? "+" : "−";
    return `${sign}${formatCurrency(Math.abs(diff))} vs. last month`;
}

export default function MonthlyReportView() {
    const [yearMonth, setYearMonth] = useState(currentYearMonth());

    const { data, isLoading, error } = useMonthlyReport(yearMonth);

    return (
        <div>
            <div className="filter-bar">
                <div className="field">
                    <label className="field__label" htmlFor="monthly-report-month">Month</label>
                    <input
                        id="monthly-report-month"
                        type="month"
                        className="input"
                        value={yearMonth}
                        onChange={(e) => setYearMonth(e.target.value)}
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
                <>
                    <div className="stat-grid">
                        <div className="card stat-card">
                            <span className="stat-card__label">Income</span>
                            <span className="stat-card__value stat-card__value--positive">
                                {formatCurrency(data.totalIncome)}
                            </span>
                            <span className="stat-card__meta">
                                {delta(data.comparisonToPreviousMonth.currentIncome, data.comparisonToPreviousMonth.previousIncome)}
                            </span>
                        </div>
                        <div className="card stat-card">
                            <span className="stat-card__label">Expense</span>
                            <span className="stat-card__value stat-card__value--negative">
                                {formatCurrency(data.totalExpense)}
                            </span>
                            <span className="stat-card__meta">
                                {delta(data.comparisonToPreviousMonth.currentExpense, data.comparisonToPreviousMonth.previousExpense)}
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
                    </div>

                    <div className="chart-grid">
                        <ChartCard
                            title="Expense by Category"
                            isEmpty={data.expenseByCategory.length === 0}
                            emptyMessage="No expenses this month."
                        >
                            <CategoryBarChart
                                data={data.expenseByCategory.map((c) => ({ name: c.categoryName, value: c.total }))}
                                valueFormatter={formatCurrency}
                            />
                        </ChartCard>

                        <ChartCard
                            title="Income by Category"
                            isEmpty={data.incomeByCategory.length === 0}
                            emptyMessage="No income this month."
                        >
                            <CategoryBarChart
                                data={data.incomeByCategory.map((c) => ({ name: c.categoryName, value: c.total }))}
                                valueFormatter={formatCurrency}
                            />
                        </ChartCard>
                    </div>
                </>
            )}
        </div>
    );
}
