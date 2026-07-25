import ChartCard from "../../components/charts/ChartCard";
import ChartLegend from "../../components/charts/ChartLegend";
import CategoryBarChart from "../../components/charts/CategoryBarChart";
import SpendingDonutChart from "../../components/charts/SpendingDonutChart";
import RecentTransactionsList from "../../components/dashboard/RecentTransactionsList";
import StatCard from "../../components/dashboard/StatCard";
import DueTransactionsPanel from "../../components/recurring/DueTransactionsPanel";
import { useDashboard } from "../../hooks/useReports";
import { formatCurrency } from "../../lib/format";

export default function DashboardPage() {
    const { data, isLoading, error } = useDashboard();

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Dashboard</h1>
                    <p className="page-header__subtitle">
                        Every figure here is derived live from your ledger — nothing is stored.
                    </p>
                </div>
            </div>

            {isLoading ? (
                <div className="card">
                    <div className="state">
                        <div className="spinner" />
                        <div className="state__desc">Loading dashboard…</div>
                    </div>
                </div>
            ) : error || !data ? (
                <div className="card">
                    <div className="state">
                        <div className="state__icon">⚠</div>
                        <div className="state__title">Couldn't load the dashboard</div>
                        <div className="state__desc">
                            Check that the backend is running on <code>localhost:8080</code> and try again.
                        </div>
                    </div>
                </div>
            ) : (
                <>
                    <div className="stat-grid">
                        <StatCard label="Total Balance" value={formatCurrency(data.totalBalance)} />
                        <StatCard label="Cash Balance" value={formatCurrency(data.cashBalance)} />
                        <StatCard label="Fund Balance" value={formatCurrency(data.totalFundBalance)} />
                        <StatCard
                            label="Loan Net Position"
                            value={formatCurrency(data.loanSummary.netPosition)}
                            tone={
                                data.loanSummary.netPosition > 0
                                    ? "positive"
                                    : data.loanSummary.netPosition < 0
                                      ? "negative"
                                      : "neutral"
                            }
                            meta={`${formatCurrency(data.loanSummary.totalReceivable)} receivable · ${formatCurrency(data.loanSummary.totalPayable)} payable`}
                        />
                        <StatCard
                            label="Income This Month"
                            value={formatCurrency(data.monthlyIncome)}
                            tone="positive"
                        />
                        <StatCard
                            label="Expense This Month"
                            value={formatCurrency(data.monthlyExpense)}
                            tone="negative"
                        />
                    </div>

                    <div className="chart-grid dashboard-section">
                        <ChartCard
                            title="Top Spending Categories"
                            subtitle="This month, by category"
                            isEmpty={data.topSpendingCategories.length === 0}
                            emptyMessage="No expenses recorded this month."
                        >
                            <div style={{ display: "flex", gap: 20, height: "100%", alignItems: "center" }}>
                                <div style={{ flex: 1, height: "100%" }}>
                                    <SpendingDonutChart
                                        data={data.topSpendingCategories.map((c) => ({
                                            name: c.categoryName,
                                            value: c.totalSpent,
                                        }))}
                                        valueFormatter={formatCurrency}
                                    />
                                </div>
                                <div style={{ flex: 1 }}>
                                    <ChartLegend
                                        data={data.topSpendingCategories.map((c) => ({
                                            name: c.categoryName,
                                            value: c.totalSpent,
                                        }))}
                                        valueFormatter={formatCurrency}
                                    />
                                </div>
                            </div>
                        </ChartCard>

                        <ChartCard title="Income vs. Expense" subtitle="This month">
                            <CategoryBarChart
                                data={[
                                    { name: "Income", value: data.monthlyIncome },
                                    { name: "Expense", value: data.monthlyExpense },
                                ]}
                                valueFormatter={formatCurrency}
                            />
                        </ChartCard>
                    </div>

                    <div className="dashboard-section">
                        <DueTransactionsPanel />
                    </div>

                    <div className="dashboard-section">
                        <h2 className="dashboard-section__title">Recent Transactions</h2>
                        <div className="card">
                            <RecentTransactionsList transactions={data.recentTransactions} />
                        </div>
                    </div>
                </>
            )}
        </>
    );
}
