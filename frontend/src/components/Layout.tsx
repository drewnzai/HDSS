import { useEffect, useState } from "react";
import { Outlet, useLocation } from "react-router-dom";
import { Menu } from "lucide-react";
import Sidebar from "./Sidebar";
import ThemeToggle from "./ThemeToggle";
import { useLogout } from "../store/useLogout";

function Layout() {
    const [collapsed, setCollapsed] = useState<boolean>(
        () => localStorage.getItem("sidebarCollapsed") === "true"
    );
    const [mobileOpen, setMobileOpen] = useState(false);
    const location = useLocation();
    const isWide = location.pathname.startsWith("/admin/users");
    const handleLogout = useLogout();

    // close the mobile drawer whenever the route changes
    useEffect(() => {
        setMobileOpen(false);
    }, [location.pathname]);

    const toggleCollapsed = () => {
        setCollapsed((prev) => {
            const next = !prev;
            localStorage.setItem("sidebarCollapsed", String(next));
            return next;
        });
    };

    return (
        <div className="app-shell">
            <Sidebar
                collapsed={collapsed}
                onToggleCollapse={toggleCollapsed}
                mobileOpen={mobileOpen}
                onCloseMobile={() => setMobileOpen(false)}
            />

            <div className="app-shell__main">
                <header className="page__header">
                    <button
                        className="mobile-menu-btn"
                        onClick={() => setMobileOpen(true)}
                        aria-label="Open menu"
                    >
                        <Menu size={20} />
                    </button>
                    <span />
                    <div style={{ display: "flex", gap: "var(--space-2)", alignItems: "center" }}>
                        <ThemeToggle />
                        <button className="btn btn--ghost" onClick={handleLogout}>
                            Sign out
                        </button>
                    </div>
                </header>

                <main className={`page__content ${isWide ? "page__content--wide" : ""}`}>
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

export default Layout;