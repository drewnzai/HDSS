import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAppSelector } from "../store/hooks";
import { selectFirstName } from "../store/AuthSlice";

interface LocationState {
    flash?: string;
    flashType?: "success" | "danger";
}

function Home() {
    const firstName = useAppSelector(selectFirstName);
    const location = useLocation();
    const navigate = useNavigate();

    const state = location.state as LocationState | null;
    const [flash, setFlash] = useState<string | null>(state?.flash ?? null);
    const [flashType] = useState<"success" | "danger">(state?.flashType ?? "success");

    useEffect(() => {
        if (!flash) return;
        const timer = setTimeout(() => setFlash(null), 4000);
        navigate(location.pathname, { replace: true, state: {} });
        return () => clearTimeout(timer);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [flash]);

    const today = new Date().toLocaleDateString(undefined, {
        weekday: "long",
        year: "numeric",
        month: "long",
        day: "numeric"
    });

    return (
        <div className="card">
            {flash && (
                <div className={`flash flash--${flashType}`} role={flashType === "danger" ? "alert" : "status"}>
                    {flash}
                </div>
            )}

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