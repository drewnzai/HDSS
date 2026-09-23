import clsx from "clsx";
import {
  ChevronLeft,
  ChevronRight,
  Home,
  LayoutDashboard,
  MapPin,
  NotepadText,
  SquareCheck,
  Users,
  X,
} from "lucide-react";
import { NavLink } from "react-router-dom";
import { selectRole, selectFirstName } from "../redux/AuthSlice";
import { useAppSelector } from "../redux/hooks";

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
  {
    label: "Forms",
    to: "/admin/forms",
    icon: NotepadText
  },
  {
    label: "Choices",
    to: "/admin/choices",
    icon: SquareCheck
  }
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
          clsx("sidebar__link", isActive && "sidebar__link--active")
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
        className={clsx(
          "sidebar",
          collapsed && "sidebar--collapsed",
          mobileOpen && "sidebar--mobile-open"
        )}
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
            {/* Always rendered now, hidden via CSS when collapsed — same
                pattern as sidebar__label. Being JS-skipped instead of
                CSS-hidden meant collapsing on desktop also silently deleted
                these from the mobile drawer, since mobile only overrides
                CSS, not the JS that decided whether the element exists. */}
            <span className="sidebar__nav-eyebrow">
              Workspace
            </span>

            {renderLinks(primaryNav)}
          </div>

          {isAdmin && (
            <div className="sidebar__nav-group">
              <span className="sidebar__nav-eyebrow">
                Administration
              </span>

              {renderLinks(adminNav)}
            </div>
          )}
        </nav>

        <div className="sidebar__footer">
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