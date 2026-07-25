import { NavLink, Outlet } from "react-router-dom";

export default function MainLayout() {
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
            </aside>

            <main className="content">
                <Outlet />
            </main>
        </div>
    );
}
