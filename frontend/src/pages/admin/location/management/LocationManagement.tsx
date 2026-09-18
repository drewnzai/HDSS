import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";


import "./location-management.css";
import type { LocationDto } from "../../../../models/Location";
import LocationExplorer from "../../../../components/location-explorer/LocationExplorer";
import PageContainer from "../../../../components/PageContainer";

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

        navigate(routerLocation.pathname, {
            replace: true,
            state: {},
        });

        return () => clearTimeout(timer);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [flash]);

    const handleAddChild = (parent: LocationDto) => {
        navigate("/admin/locations/create", {
            state: { parent },
        });
    };

    const handleAddRoot = () => {
        navigate("/admin/locations/create", {
            state: { parent: null },
        });
    };

    return (
        <PageContainer size="wide">
            <div className="location-management">
                {flash && (
                    <div
                        className="location-management__flash"
                        role="status"
                    >
                        {flash}
                    </div>
                )}

                <header className="location-management__header">
                    <div className="location-management__heading">

                        <h1 className="location-management__title">
                            Locations
                        </h1>

                        <p className="location-management__description">
                            Manage the geographic hierarchy used throughout
                            the system.
                        </p>
                    </div>

                    <div className="location-management__actions">
                        <button
                            type="button"
                            className="location-management__import"
                            onClick={() =>
                                navigate("/admin/locations/import")
                            }
                        >
                            Import from spreadsheet
                        </button>

                        <button
                            type="button"
                            className="location-management__create"
                            onClick={handleAddRoot}
                        >
                            Add country
                        </button>
                    </div>
                </header>

                <section className="location-management__section">
                    <header className="location-management__section-header">

                        <h2 className="location-management__section-title">
                            Geographic structure
                        </h2>

                        <p className="location-management__section-description">
                            Expand a location to view its immediate children.
                            Use the plus button to add the next level.
                        </p>
                    </header>

                    <LocationExplorer
                        onAddChild={handleAddChild}
                        onAddRoot={handleAddRoot}
                    />
                </section>
            </div>
        </PageContainer>
    );
}

export default LocationManagement;