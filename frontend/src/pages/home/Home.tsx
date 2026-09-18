import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Download, Database, MapPin, Users, Activity } from "lucide-react";
import { selectFirstName } from "../../store/AuthSlice";
import { useAppSelector } from "../../store/hooks";
import "./home.css"

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
    const [flashType] = useState<"success" | "danger">(
        state?.flashType ?? "success"
    );

    useEffect(() => {
        if (!flash) return;

        const timer = setTimeout(() => setFlash(null), 4000);

        navigate(location.pathname, {
            replace: true,
            state: {},
        });

        return () => clearTimeout(timer);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [flash]);

    return (
        <div className="home-page">
            {flash && (
                <div
                    className={`flash flash--${ flashType } `}
                    role={flashType === "danger" ? "alert" : "status"}
                >
                    {flash}
                </div>
            )}

            <section className="home-hero">
                <div className="home-hero__eyebrow">
                    Health & Demographics Surveillance System
                </div>

                <h1 className="home-hero__title">
                    Welcome{firstName ? `, ${ firstName } ` : ""}
                </h1>

                <p className="home-hero__description">
                    Access the HDSS field portal, download the mobile
                    application, and learn more about how health and
                    demographic surveillance supports evidence-based
                    decision-making.
                </p>

                <a
                    className="home-download"
                    href="/api/mobile-app/download"
                    download
                >
                    <Download size={20} aria-hidden="true" />

                    <span>
                        <strong>Download the Android App</strong>
                        <small>Download the latest field data collection app</small>
                    </span>
                </a>
            </section>

            <section className="home-section">
                <div className="home-section__heading">
                    <span className="home-section__eyebrow">
                        About the system
                    </span>

                    <h2>What is a HDSS?</h2>

                    <p>
                        A Health and Demographics Surveillance System (HDSS)
                        is a long-term population-based platform that
                        continuously monitors the health, demographic, and
                        social characteristics of a defined population.
                    </p>
                </div>

                <div className="home-info-grid">
                    <article className="home-info-card">
                        <div className="home-info-card__icon">
                            <Users size={20} aria-hidden="true" />
                        </div>

                        <h3>Population dynamics</h3>

                        <p>
                            HDSS platforms track important demographic events
                            such as births, deaths, migration, household
                            changes, and population growth over time.
                        </p>
                    </article>

                    <article className="home-info-card">
                        <div className="home-info-card__icon">
                            <Activity size={20} aria-hidden="true" />
                        </div>

                        <h3>Health monitoring</h3>

                        <p>
                            Repeated data collection helps researchers and
                            public health teams understand patterns in health,
                            disease, mortality, and other population-level
                            outcomes.
                        </p>
                    </article>

                    <article className="home-info-card">
                        <div className="home-info-card__icon">
                            <MapPin size={20} aria-hidden="true" />
                        </div>

                        <h3>Defined communities</h3>

                        <p>
                            Surveillance follows people and households within
                            a defined geographic area, creating a consistent
                            picture of population change across time.
                        </p>
                    </article>

                    <article className="home-info-card">
                        <div className="home-info-card__icon">
                            <Database size={20} aria-hidden="true" />
                        </div>

                        <h3>Evidence for action</h3>

                        <p>
                            Reliable longitudinal data can support research,
                            programme planning, resource allocation, and the
                            evaluation of health and development interventions.
                        </p>
                    </article>
                </div>
            </section>

            <section className="home-note">
                <div>
                    <span className="home-note__eyebrow">
                        Field data collection
                    </span>

                    <h2>Keep your field teams connected.</h2>

                    <p>
                        Use the HDSS Android application to collect and submit
                        surveillance data from the field. Make sure you are
                        using the current version provided by the system.
                    </p>
                </div>

                <a
                    className="btn btn--primary"
                    href="/api/mobile-app/download"
                    download
                >
                    <Download size={18} aria-hidden="true" />
                    Download App
                </a>
            </section>
        </div>
    );
}

export default Home;