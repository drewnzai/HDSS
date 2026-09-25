import { Moon, Sun } from "lucide-react";
import { useTheme } from "../../theme/ThemeProvider";
import "./theme-toggle.css";

function ThemeToggle() {
    const { theme, toggleTheme } = useTheme();

    const isDark = theme === "dark";

    return (
        <button
            type="button"
            className="theme-toggle"
            onClick={toggleTheme}
            aria-label={
                isDark
                    ? "Switch to light theme"
                    : "Switch to dark theme"
            }
            title={
                isDark
                    ? "Switch to light theme"
                    : "Switch to dark theme"
            }
        >
            {isDark ? (
                <Sun size={18} aria-hidden="true" />
            ) : (
                <Moon size={18} aria-hidden="true" />
            )}
        </button>
    );
}

export default ThemeToggle;