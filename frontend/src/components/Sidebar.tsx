import {
  ChevronLeft,
  ChevronRight,
  Home,
  LayoutDashboard,
  MapPin,
  Menu,
  Users,
  X,
} from "lucide-react";
import { NavLink } from "react-router-dom";
import { selectRole, selectFirstName } from "../store/AuthSlice";
import { useAppSelector } from "../store/hooks";

interface SidebarProps {
  collapsed: boolean;
  mobileOpen: boolean;
  onToggleCollapsed: () => void;
  onOpenMobile: () => void;
  onCloseMobile: () => void;
}

interface NavItem {
  label: string;
  to: string;
  icon: typeof Home;
  end?: boolean;
}

const primaryNav: NavItem[] = [
  {
    label: "Home",
    to: "/",
    icon: Home,
    end: true,
  },
  {
    label: "Dashboard",
    to: "/dashboard",
    icon: LayoutDashboard,
  },
];

const adminNav: NavItem[] = [
  {
    label: "User management",
    to: "/admin/users",
    icon: Users,
  },
  {
    label: "Locations",
    to: "/admin/locations",
    icon: MapPin,
  },
];

function Sidebar({
  collapsed,
  mobileOpen,
  onToggleCollapsed,
  onOpenMobile,
  onCloseMobile,
}: SidebarProps) {
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
        className={({ isActive }) =>
          [
            "sidebar__link",
            isActive ? "sidebar__link--active" : "",
          ]
            .filter(Boolean)
            .join(" ")
        }
        title={collapsed ? label : undefined}
      >
        <Icon
          size={18}
          strokeWidth={2}
          className="sidebar__icon"
          aria-hidden="true"
        />

        <span className="sidebar__label">
          {label}
        </span>
      </NavLink>
    ));

  return (
    <>
      {/* Mobile menu trigger */}
      <button
        type="button"
        className="sidebar__mobile-trigger"
        onClick={onOpenMobile}
        aria-label="Open navigation menu"
        aria-expanded={mobileOpen}
      >
        <Menu size={20} aria-hidden="true" />
      </button>

      {/* Mobile backdrop */}
      {mobileOpen && (
        <button
          type="button"
          className="sidebar__backdrop"
          onClick={onCloseMobile}
          aria-label="Close navigation menu"
        />
      )}

      <aside
        className={[
          "sidebar",
          collapsed ? "sidebar--collapsed" : "",
          mobileOpen ? "sidebar--mobile-open" : "",
        ]
          .filter(Boolean)
          .join(" ")}
      >
        <div className="sidebar__header">
          <div className="sidebar__brand">
            <span className="sidebar__brand-mark">
              H
            </span>

            <span className="sidebar__brand-text">
              HDSS
            </span>
          </div>

          {/* Mobile close */}
          <button
            type="button"
            className="sidebar__mobile-close"
            onClick={onCloseMobile}
            aria-label="Close navigation menu"
          >
            <X size={20} aria-hidden="true" />
          </button>
        </div>

        <nav className="sidebar__nav" aria-label="Primary navigation">
          <div className="sidebar__nav-group">
            {!collapsed && (
              <span className="sidebar__nav-eyebrow">
                Workspace
              </span>
            )}

            {renderLinks(primaryNav)}
          </div>

          {isAdmin && (
            <div className="sidebar__nav-group">
              {!collapsed && (
                <span className="sidebar__nav-eyebrow">
                  Administration
                </span>
              )}

              {renderLinks(adminNav)}
            </div>
          )}
        </nav>

        <div className="sidebar__footer">
          {!collapsed && (
            <div className="sidebar__user">
              <div className="sidebar__avatar">
                {firstName?.charAt(0).toUpperCase() ?? "U"}
              </div>

              <div className="sidebar__user-info">
                <span className="sidebar__user-name">
                  {firstName || "User"}
                </span>

                <span className="sidebar__user-role">
                  {role}
                </span>
              </div>
            </div>
          )}

          <button
            type="button"
            className="sidebar__toggle"
            onClick={onToggleCollapsed}
            aria-label={
              collapsed
                ? "Expand sidebar"
                : "Collapse sidebar"
            }
            title={
              collapsed
                ? "Expand sidebar"
                : "Collapse sidebar"
            }
          >
            {collapsed ? (
              <ChevronRight
                size={17}
                aria-hidden="true"
              />
            ) : (
              <ChevronLeft
                size={17}
                aria-hidden="true"
              />
            )}
          </button>
        </div>
      </aside>
    </>
  );
}

export default Sidebar;