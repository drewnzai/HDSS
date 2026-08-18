import { useGetChildrenQuery } from "../store/LocationApi";
import LocationTreeNode from "./LocationTreeNode";
import type { LocationDto } from "../models/Location";

interface LocationTreeProps {
    onAddChild: (parent: LocationDto) => void;
    onAddRoot: () => void;
}

function LocationTree({ onAddChild, onAddRoot }: LocationTreeProps) {
    const { data: roots, isLoading, error } = useGetChildrenQuery(null);

    if (isLoading) return <p>Loading locations…</p>;
    if (error) return <p className="field__error">Couldn't load locations.</p>;

    return (
        <div className="location-tree">
            {roots && roots.length === 0 && (
                <div className="location-node__empty">
                    No locations yet.{" "}
                    <button className="btn btn--ghost btn--sm" onClick={onAddRoot}>
                        Add a country
                    </button>
                </div>
            )}
            {roots?.map((root) => (
                <LocationTreeNode key={root.id} location={root} depth={0} onAddChild={onAddChild} />
            ))}
        </div>
    );
}

export default LocationTree;