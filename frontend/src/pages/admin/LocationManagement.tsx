import { useLocation, useNavigate } from "react-router-dom";
import LocationTree from "../../components/LocationTree";
import type { LocationDto } from "../../models/Location";
import { useState, useEffect } from "react";

interface NavState {
    flash?: string;
}

function LocationManagement() {
    const navigate = useNavigate();
    const routerLocation = useLocation();

    const [flash, setFlash] = useState<string | null>(
        (routerLocation.state as NavState | null)?.flash ?? null
    );

    useEffect(() => {
        if (!flash) return;
        const timer = setTimeout(() => setFlash(null), 4000);
        navigate(routerLocation.pathname, { replace: true, state: {} });
        return () => clearTimeout(timer);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [flash]);

    const handleAddChild = (parent: LocationDto) => {
        navigate("/admin/locations/create", { state: { parent } });
    };

    const handleAddRoot = () => {
        navigate("/admin/locations/create", { state: { parent: null } });
    };

    return (
        <div className="card card--wide">
            {flash && <div className="flash flash--success" role="status">{flash}</div>}
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                <div>
                    <span className="ledger-section__eyebrow">§ Admin — Location Management</span>
                    <h1>Locations</h1>
                </div>
                <div style={{ display: "flex", gap: "var(--space-2)" }}>
                    <button className="btn btn--ghost" onClick={() => navigate("/admin/locations/import")}>
                        Import from spreadsheet
                    </button>
                    <button className="btn btn--primary" onClick={handleAddRoot}>
                        Add country
                    </button>
                </div>
            </div>

            <div className="ledger-section">
                <span className="ledger-section__eyebrow">§ 01 — Hierarchy</span>
                <LocationTree onAddChild={handleAddChild} onAddRoot={handleAddRoot} />
            </div>
        </div>
    );
}

export default LocationManagement;