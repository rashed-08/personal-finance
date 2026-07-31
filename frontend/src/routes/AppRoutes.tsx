import { Navigate, Route, Routes } from "react-router-dom";

import MainLayout from "../layouts/MainLayout";
import AuthLayout from "../layouts/AuthLayout";
import ProtectedRoute from "./ProtectedRoute";
import LoginPage from "../app/login/page";
import RegisterPage from "../app/register/page";
import ForgotPasswordPage from "../app/forgot-password/page";
import ResetPasswordPage from "../app/reset-password/page";
import VerifyEmailPage from "../app/verify-email/page";
import ProfilePage from "../app/profile/page";
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
import ImportPage from "../app/import/page";

export default function AppRoutes() {
    return (
        <Routes>
            {/* Public — signed-in visitors are bounced to the dashboard */}
            <Route element={<AuthLayout />}>
                <Route
                    path="login"
                    element={<LoginPage />}
                />

                <Route
                    path="register"
                    element={<RegisterPage />}
                />

                <Route
                    path="forgot-password"
                    element={<ForgotPasswordPage />}
                />

                <Route
                    path="reset-password"
                    element={<ResetPasswordPage />}
                />
            </Route>

            {/* Works signed in or out: a new user follows this from their inbox */}
            <Route element={<AuthLayout redirectIfAuthenticated={false} />}>
                <Route
                    path="verify-email"
                    element={<VerifyEmailPage />}
                />
            </Route>

            {/* Everything below requires authentication */}
            <Route element={<ProtectedRoute />}>
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

                    <Route
                        path="import"
                        element={<ImportPage />}
                    />

                    <Route
                        path="profile"
                        element={<ProfilePage />}
                    />

                </Route>
            </Route>
        </Routes>
    );
}
