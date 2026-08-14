import { Link } from "react-router-dom";
import { useAppSelector } from "../store/hooks";
import { selectUsername } from "../store/AuthSlice";
import { useLogout } from "../store/useLogout";
import ThemeToggle from "../components/ThemeToggle";

function Home() {
    const username = useAppSelector(selectUsername);
    const handleLogout = useLogout();

    const today = new Date().toLocaleDateString(undefined, {
        weekday: "long",
        year: "numeric",
        month: "long",
        day: "numeric"
    });

    return (
        <div className="page">
            <header className="page__header">
                <span className="record-id">HDSS · Field Portal</span>
                <ThemeToggle />
            </header>

            <main className="page__content">
                <div className="card">
                    <span className="ledger-section__eyebrow">§ 00 — Session</span>
                    <h1>Welcome back{username ? `, ${username}` : ""}</h1>
                    <p>{today}</p>

                    <div className="ledger-section">
                        <span className="ledger-section__eyebrow">§ 01 — Navigate</span>
                        <div className="field-group">
                            <Link to="/dashboard" className="btn btn--primary btn--full">
                                Go to dashboard
                            </Link>
                            <button className="btn btn--ghost btn--full" onClick={handleLogout}>
                                Sign out
                            </button>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default Home;