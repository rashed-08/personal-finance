import { Navigate, Outlet } from "react-router-dom";

import { useAuth } from "../hooks/useAuth";

interface Props {
    /**
     * Send already-signed-in visitors to the dashboard. False for pages that
     * work in both states — e.g. email verification, which a freshly
     * registered (and therefore signed-in) user follows from their inbox.
     */
    redirectIfAuthenticated?: boolean;
}

/**
 * Shell for the pages outside the app proper (login, register, password
 * reset, email verification).
 */
export default function AuthLayout({ redirectIfAuthenticated = true }: Props) {
    const { isAuthenticated, isLoading } = useAuth();

    if (isLoading) {
        return (
            <div className="auth-bootstrap">
                <div className="spinner" />
            </div>
        );
    }

    if (isAuthenticated && redirectIfAuthenticated) {
        return <Navigate to="/dashboard" replace />;
    }

    return (
        <div className="auth-shell">
            <div className="auth-card">
                <div className="auth-brand">
                    <div className="brand__mark">৳</div>
                    <div>
                        <div className="brand__name">Personal Finance</div>
                        <div className="brand__sub">Money, tracked</div>
                    </div>
                </div>

                <Outlet />
            </div>
        </div>
    );
}
