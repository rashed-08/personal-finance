import { Navigate, Route, Routes } from "react-router-dom";

import MainLayout from "../layouts/MainLayout";
import DashboardPage from "../app/dashboard/page";
import ReportsPage from "../app/reports/page";
import AccountsPage from "../app/accounts/page";
import CategoriesPage from "../app/categories/page";
import TransactionsPage from "../app/transactions/page";
import SalaryCyclesPage from "../app/salary-cycles/page";
import CashReconciliationPage from "../app/cash-reconciliation/page";
import FundsPage from "../app/funds/page";
import LoansPage from "../app/loans/page";
import RecurringTransactionsPage from "../app/recurring-transactions/page";

export default function AppRoutes() {
    return (
        <Routes>
            <Route path="/" element={<MainLayout />}>
                <Route
                    index
                    element={<Navigate to="/dashboard" replace />}
                />

                <Route
                    path="dashboard"
                    element={<DashboardPage />}
                />

                <Route
                    path="reports"
                    element={<ReportsPage />}
                />

                <Route
                    path="accounts"
                    element={<AccountsPage />}
                />

                <Route
                    path="categories"
                    element={<CategoriesPage />}
                />

                <Route
                    path="transactions"
                    element={<TransactionsPage />}
                />

                <Route
                    path="salary-cycles"
                    element={<SalaryCyclesPage />}
                />

                <Route
                    path="cash-reconciliation"
                    element={<CashReconciliationPage />}
                />

                <Route
                    path="funds"
                    element={<FundsPage />}
                />

                <Route
                    path="loans"
                    element={<LoansPage />}
                />

                <Route
                    path="recurring-transactions"
                    element={<RecurringTransactionsPage />}
                />

            </Route>
        </Routes>
    );
}