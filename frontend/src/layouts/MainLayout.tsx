import { NavLink, Outlet, useNavigate } from "react-router-dom";

import { useAuth } from "../hooks/useAuth";

export default function MainLayout() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    async function handleLogout() {
        await logout();
        navigate("/login", { replace: true });
    }

    return (
        <div className="app-shell">
            <aside className="sidebar">
                <div className="brand">
                    <div className="brand__mark">৳</div>
                    <div>
                        <div className="brand__name">Personal Finance</div>
                        <div className="brand__sub">Money, tracked</div>
                    </div>
                </div>

                <nav className="nav">
                    <div className="nav__label">Overview</div>

                    <NavLink
                        to="/dashboard"
                        className={({ isActive }) =>
                            isActive ? "nav__item nav__item--active" : "nav__item"
                        } >
                        📊 Dashboard
                    </NavLink>

                    <NavLink
                        to="/reports"
                        className={({ isActive }) =>
                            isActive ? "nav__item nav__item--active" : "nav__item"
                        } >
                        📈 Reports
                    </NavLink>

                    <div className="nav__label">Manage</div>

                    <NavLink
                        to="/accounts"
                        className={({ isActive }) =>
                            isActive ? "nav__item nav__item--active" : "nav__item"
                        } >
                        🏦 Accounts
                    </NavLink>

                    <NavLink
                        to="/categories"
                        className={({ isActive }) =>
                            isActive ? "nav__item nav__item--active" : "nav__item"
                        } >
                        📂 Categories
                    </NavLink>

                    <NavLink
                        to="/transactions"
                        className={({ isActive }) =>
                            isActive ? "nav__item nav__item--active" : "nav__item"
                        } >
                        🧾 Transactions
                    </NavLink>

                    <NavLink
                        to="/salary-cycles"
                        className={({ isActive }) =>
                            isActive ? "nav__item nav__item--active" : "nav__item"
                        } >
                        📅 Salary Cycles
                    </NavLink>

                    <NavLink
                        to="/cash-reconciliation"
                        className={({ isActive }) =>
                            isActive ? "nav__item nav__item--active" : "nav__item"
                        } >
                        💵 Cash Reconciliation
                    </NavLink>

                    <NavLink
                        to="/funds"
                        className={({ isActive }) =>
                            isActive ? "nav__item nav__item--active" : "nav__item"
                        } >
                        🎯 Funds
                    </NavLink>

                    <NavLink
                        to="/loans"
                        className={({ isActive }) =>
                            isActive ? "nav__item nav__item--active" : "nav__item"
                        } >
                        🤝 Loans
                    </NavLink>

                    <NavLink
                        to="/recurring-transactions"
                        className={({ isActive }) =>
                            isActive ? "nav__item nav__item--active" : "nav__item"
                        } >
                        🔁 Recurring
                    </NavLink>

                    <div className="nav__label">Data</div>

                    <NavLink
                        to="/import"
                        className={({ isActive }) =>
                            isActive ? "nav__item nav__item--active" : "nav__item"
                        } >
                        📥 Import
                    </NavLink>
                </nav>

                <div className="sidebar__footer">
                    <NavLink
                        to="/profile"
                        className={({ isActive }) =>
                            isActive ? "user-chip user-chip--active" : "user-chip"
                        } >
                        <span className="user-chip__avatar">
                            {user?.name?.charAt(0).toUpperCase() ?? "?"}
                        </span>
                        <span className="user-chip__text">
                            <span className="user-chip__name">{user?.name}</span>
                            <span className="user-chip__email">{user?.email}</span>
                        </span>
                    </NavLink>

                    <button
                        type="button"
                        className="btn btn--ghost btn--sm btn--block"
                        onClick={handleLogout}
                    >
                        Sign Out
                    </button>
                </div>
            </aside>

            <main className="content">
                <Outlet />
            </main>
        </div>
    );
}
