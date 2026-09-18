import { useEffect, useState } from "react";
import { Menu } from "lucide-react";
import { Outlet, useLocation } from "react-router-dom";

import Sidebar from "../Sidebar";
import "./layout.css";

function Layout() {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(() => {
    return localStorage.getItem("sidebarCollapsed") === "true";
  });

  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);

  const location = useLocation();

  useEffect(() => {
    setMobileSidebarOpen(false);
  }, [location.pathname]);

  const handleToggleCollapse = () => {
    setSidebarCollapsed((current) => {
      const next = !current;

      localStorage.setItem(
        "sidebarCollapsed",
        String(next)
      );

      return next;
    });
  };

  const handleOpenMobileSidebar = () => {
    setMobileSidebarOpen(true);
  };

  const handleCloseMobileSidebar = () => {
    setMobileSidebarOpen(false);
  };

  return (
    <div
      className={["app-shell", sidebarCollapsed ? "app-shell--collapsed" : "",].filter(Boolean).join(" ")} >
      <Sidebar
        collapsed={sidebarCollapsed}
        mobileOpen={mobileSidebarOpen}
        onToggleCollapsed={handleToggleCollapse}
        onOpenMobile={handleOpenMobileSidebar}
        onCloseMobile={handleCloseMobileSidebar} />

      <main className="app-content">
        <button
          type="button"
          className="app-content__mobile-menu"
          onClick={handleOpenMobileSidebar}
          aria-label="Open navigation menu"
          aria-expanded={mobileSidebarOpen} >
          <Menu size={20} aria-hidden="true" />

        </button>
        <Outlet />
      </main>
    </div>
  );
}

export default Layout;