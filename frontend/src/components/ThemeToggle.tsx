import { useTheme } from "../theme/ThemeContext";

function ThemeToggle() {
    const { theme, toggleTheme } = useTheme();

    return (
        <button
            className="btn btn--ghost"
            onClick={toggleTheme}
            aria-label={`Switch to ${theme === "light" ? "dark" : "light"} mode`}
        >
            {theme === "light" ? "Dark mode" : "Light mode"}
        </button>
    );
}

export default ThemeToggle;