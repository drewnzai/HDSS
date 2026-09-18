import type { LocationDto } from "../../models/Location";
import { useGetChildrenQuery } from "../../store/LocationApi";
import LocationExplorerNode from "./LocationExplorerNode";
import "./location-explorer.css";

interface LocationExplorerProps {
    onAddChild: (parent: LocationDto) => void;
    onAddRoot: () => void;
}

function LocationExplorer({
    onAddChild,
    onAddRoot,
}: LocationExplorerProps) {
    const {
        data: roots,
        isLoading,
        error,
    } = useGetChildrenQuery(null);

    if (isLoading) {
        return (
            <div className="location-explorer__state">
                <span
                    className="location-explorer__spinner"
                    aria-hidden="true"
                />
                <span>Loading locations...</span>
            </div>
        );
    }

    if (error) {
        return (
            <div className="location-explorer__state location-explorer__state--error">
                <span className="location-explorer__state-eyebrow">
                    Locations unavailable
                </span>

                <p className="location-explorer__state-message">
                    Couldn't load the location hierarchy.
                </p>

                <button
                    type="button"
                    className="location-explorer__state-action"
                    onClick={onAddRoot}
                >
                    Add a country
                </button>
            </div>
        );
    }

    if (!roots || roots.length === 0) {
        return (
            <div className="location-explorer__empty">
                <div>
                    <span className="location-explorer__empty-eyebrow">
                        No locations
                    </span>

                    <p className="location-explorer__empty-message">
                        There are no countries in the location hierarchy yet.
                    </p>
                </div>

                <button
                    type="button"
                    className="location-explorer__empty-action"
                    onClick={onAddRoot}
                >
                    Add a country
                </button>
            </div>
        );
    }

    return (
        <div className="location-explorer">
            <div className="location-explorer__legend">
                <span>Location</span>
                <span>Type</span>
                <span>Code</span>
                <span className="location-explorer__legend-actions">
                    Actions
                </span>
            </div>

            <div className="location-explorer__nodes">
                {roots.map((location) => (
                    <LocationExplorerNode
                        key={location.id}
                        location={location}
                        depth={0}
                        onAddChild={onAddChild}
                    />
                ))}
            </div>
        </div>
    );
}

export default LocationExplorer;