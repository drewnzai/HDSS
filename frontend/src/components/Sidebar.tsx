import { NavLink } from "react-router-dom";
import { Home, LayoutDashboard, Users, ChevronLeft, ChevronRight, X } from "lucide-react";
import { useAppSelector } from "../store/hooks";
import { selectRole, selectFirstName } from "../store/AuthSlice";
import type { LucideIcon } from "lucide-react";

interface SidebarProps {
    collapsed: boolean;
    onToggleCollapse: () => void;
    mobileOpen: boolean;
    onCloseMobile: () => void;
}

interface NavItem {
    label: string;
    to: string;
    icon: LucideIcon;
    end?: boolean;
}

const primaryNav: NavItem[] = [
    { label: "Home", to: "/", icon: Home, end: true },
    { label: "Dashboard", to: "/dashboard", icon: LayoutDashboard }
];

const adminNav: NavItem[] = [
    { label: "User management", to: "/admin/users", icon: Users }
];

function Sidebar({ collapsed, onToggleCollapse, mobileOpen, onCloseMobile }: SidebarProps) {
    const role = useAppSelector(selectRole);
    const firstName = useAppSelector(selectFirstName);
    const isAdmin = role === "ADMIN";

    const renderLinks = (items: NavItem[]) =>
        items.map(({ label, to, icon: Icon, end }) => (
            <NavLink
                key={to}
                to={to}
                end={end}
                onClick={onCloseMobile}
                className={({ isActive }) => `sidebar__link ${isActive ? "sidebar__link--active" : ""}`}
                title={collapsed ? label : undefined}
            >
                <Icon size={18} strokeWidth={2} className="sidebar__icon" />
                <span className="sidebar__label">{label}</span>
            </NavLink>
        ));

    return (
        <>
            {mobileOpen && <div className="sidebar__backdrop" onClick={onCloseMobile} />}

            <aside className={`sidebar ${collapsed ? "sidebar--collapsed" : ""} ${mobileOpen ? "sidebar--mobile-open" : ""}`}>
                <div className="sidebar__brand">
                    {!collapsed && <span className="record-id">HDSS</span>}
                    <button
                        className="sidebar__toggle sidebar__toggle--desktop"
                        onClick={onToggleCollapse}
                        aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
                    >
                        {collapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
                    </button>
                    <button
                        className="sidebar__toggle sidebar__toggle--mobile"
                        onClick={onCloseMobile}
                        aria-label="Close menu"
                    >
                        <X size={18} />
                    </button>
                </div>

                <nav className="sidebar__nav">
                    <div className="sidebar__section">
                        {!collapsed && <span className="sidebar__section-label">§ Navigate</span>}
                        {renderLinks(primaryNav)}
                    </div>

                    {isAdmin && (
                        <div className="sidebar__section">
                            {!collapsed && <span className="sidebar__section-label">§ Admin</span>}
                            {renderLinks(adminNav)}
                        </div>
                    )}
                </nav>

                {!collapsed && firstName && (
                    <div className="sidebar__footer">
                        <span className="record-id">Signed in as {firstName}</span>
                    </div>
                )}
            </aside>
        </>
    );
}

export default Sidebar;