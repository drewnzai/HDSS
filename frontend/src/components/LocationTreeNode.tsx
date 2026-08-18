import { useState } from "react";
import { ChevronRight, ChevronDown, Plus, Loader2 } from "lucide-react";
import { useGetChildrenQuery } from "../store/LocationApi";
import { childTypeOf, LOCATION_TYPE_LABELS } from "../models/Location";
import type { LocationDto} from "../models/Location";

interface LocationTreeNodeProps {
    location: LocationDto;
    depth: number;
    onAddChild: (parent: LocationDto) => void;
}

function LocationTreeNode({ location, depth, onAddChild }: LocationTreeNodeProps) {
    const [expanded, setExpanded] = useState(false);
    const canHaveChildren = childTypeOf(location.type) !== null;

    const { data: children, isFetching } = useGetChildrenQuery(location.id, {
        skip: !expanded
    });

    return (
        <div className="location-node">
            <div className="location-node__row" style={{ paddingLeft: depth * 20 }}>
                <button
                    className="location-node__toggle"
                    onClick={() => setExpanded((prev) => !prev)}
                    disabled={!canHaveChildren}
                    aria-label={expanded ? "Collapse" : "Expand"}
                >
                    {canHaveChildren ? (
                        expanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />
                    ) : (
                        <span className="location-node__leaf-dot" />
                    )}
                </button>

                <span className="location-node__name">{location.name}</span>
                <span className="location-node__type">{LOCATION_TYPE_LABELS[location.type]}</span>
                {location.code && <span className="record-id location-node__code">{location.code}</span>}

                {canHaveChildren && (
                    <button
                        className="location-node__add"
                        onClick={() => onAddChild(location)}
                        title={`Add ${LOCATION_TYPE_LABELS[childTypeOf(location.type)!]}`}
                    >
                        <Plus size={14} />
                    </button>
                )}
            </div>

            {expanded && (
                <div className="location-node__children">
                    {isFetching && (
                        <div className="location-node__loading" style={{ paddingLeft: (depth + 1) * 20 }}>
                            <Loader2 size={14} className="spin" /> Loading…
                        </div>
                    )}
                    {!isFetching && children?.length === 0 && (
                        <div className="location-node__empty" style={{ paddingLeft: (depth + 1) * 20 }}>
                            No {LOCATION_TYPE_LABELS[childTypeOf(location.type)!]} records yet.
                        </div>
                    )}
                    {children?.map((child) => (
                        <LocationTreeNode
                            key={child.id}
                            location={child}
                            depth={depth + 1}
                            onAddChild={onAddChild}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}

export default LocationTreeNode;