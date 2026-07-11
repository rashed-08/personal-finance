import { Navigate, Route, Routes } from "react-router-dom";

import MainLayout from "../layouts/MainLayout";
import AccountsPage from "../app/accounts/page";

export default function AppRoutes() {
    return (
        <Routes>
            <Route path="/" element={<MainLayout />}>
                <Route
                    index
                    element={<Navigate to="/accounts" replace />}
                />

                <Route
                    path="accounts"
                    element={<AccountsPage />}
                />

            </Route>
        </Routes>
    );
}