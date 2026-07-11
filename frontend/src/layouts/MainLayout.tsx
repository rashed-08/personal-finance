import { Link, Outlet } from "react-router-dom";

export default function MainLayout() {
    return (
        <div style={{ display: "flex", minHeight: "100vh" }}>
            <aside
                style={{
                    width: "240px",
                    padding: "20px",
                    borderRight: "1px solid #ddd",
                }}
            >
                <h2>Personal Finance</h2>

                <nav>
                    <ul style={{ listStyle: "none", padding: 0 }}>
                        <li>
                            <Link to="/accounts">Accounts</Link>
                        </li>

                        <li>
                            <Link to="/categories">Categories</Link>
                        </li>
                    </ul>
                </nav>
            </aside>

            <main style={{ flex: 1, padding: "24px" }}>
                <Outlet />
            </main>
        </div>
    );
}