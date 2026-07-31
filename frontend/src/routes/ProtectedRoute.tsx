import { Navigate, Outlet, useLocation } from "react-router-dom";

import { useAuth } from "../hooks/useAuth";

export default function ProtectedRoute() {
    const { isAuthenticated, isLoading } = useAuth();
    const location = useLocation();

    if (isLoading) {
        return (
            <div className="auth-bootstrap">
                <div className="spinner" />
            </div>
        );
    }

    if (!isAuthenticated) {
        // Remember where the user was headed so login can send them back.
        return <Navigate to="/login" replace state={{ from: location }} />;
    }

    return <Outlet />;
}
