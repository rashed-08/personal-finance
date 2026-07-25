import { useState } from "react";

import AccountsReportView from "../../components/reports/AccountsReportView";
import CashFlowReportView from "../../components/reports/CashFlowReportView";
import CategoryReportView from "../../components/reports/CategoryReportView";
import FundsReportView from "../../components/reports/FundsReportView";
import IncomeExpenseReportView from "../../components/reports/IncomeExpenseReportView";
import LoansReportView from "../../components/reports/LoansReportView";
import MonthlyReportView from "../../components/reports/MonthlyReportView";
import RecurringReportView from "../../components/reports/RecurringReportView";
import SalaryCycleReportView from "../../components/reports/SalaryCycleReportView";

const TABS = [
    { key: "income-expense", label: "Income & Expense", Component: IncomeExpenseReportView },
    { key: "category", label: "Category", Component: CategoryReportView },
    { key: "monthly", label: "Monthly", Component: MonthlyReportView },
    { key: "cash-flow", label: "Cash Flow", Component: CashFlowReportView },
    { key: "accounts", label: "Accounts", Component: AccountsReportView },
    { key: "funds", label: "Funds", Component: FundsReportView },
    { key: "loans", label: "Loans", Component: LoansReportView },
    { key: "salary-cycles", label: "Salary Cycles", Component: SalaryCycleReportView },
    { key: "recurring", label: "Recurring", Component: RecurringReportView },
] as const;

export default function ReportsPage() {
    const [activeKey, setActiveKey] = useState<(typeof TABS)[number]["key"]>("income-expense");

    const ActiveView = TABS.find((t) => t.key === activeKey)?.Component ?? IncomeExpenseReportView;

    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Reports</h1>
                    <p className="page-header__subtitle">
                        Every figure here is derived live from your ledger — nothing is stored.
                    </p>
                </div>
            </div>

            <div className="tabs">
                {TABS.map((tab) => (
                    <button
                        key={tab.key}
                        type="button"
                        className={activeKey === tab.key ? "tab tab--active" : "tab"}
                        onClick={() => setActiveKey(tab.key)}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            <ActiveView />
        </>
    );
}
