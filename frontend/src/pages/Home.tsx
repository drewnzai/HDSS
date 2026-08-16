import { Link } from "react-router-dom";
import { useAppSelector } from "../store/hooks";
import { selectFirstName} from "../store/AuthSlice";
import { useLogout } from "../store/useLogout";
import ThemeToggle from "../components/ThemeToggle";

function Home() {
    const firstName = useAppSelector(selectFirstName);
    const handleLogout = useLogout();

    const today = new Date().toLocaleDateString(undefined, {
        weekday: "long",
        year: "numeric",
        month: "long",
        day: "numeric"
    });

    return (
        <div className="card">
            <span className="ledger-section__eyebrow">§ 00 — Session</span>
            <h1>Welcome back{firstName ? `, ${firstName}` : ""}</h1>
            <p>{today}</p>

            <div className="ledger-section">
                <span className="ledger-section__eyebrow">§ 01 — Navigate</span>
                <Link to="/dashboard" className="btn btn--primary">
                    Go to dashboard
                </Link>
            </div>
        </div>
    );
}

export default Home;