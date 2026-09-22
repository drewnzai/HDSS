import { ChevronDown, ChevronRight, Plus, Loader2 } from "lucide-react";
import { useState } from "react";
import { type LocationDto, childTypeOf, LOCATION_TYPE_LABELS } from "../../models/Location";
import { useGetChildrenQuery } from "../../redux/LocationApi";

interface LocationExplorerNodeProps {
    location: LocationDto;
    depth: number;
    onAddChild: (parent: LocationDto) => void;
}

function LocationExplorerNode({
    location,
    depth,
    onAddChild,
}: LocationExplorerNodeProps) {
    const [expanded, setExpanded] = useState(false);

    const childType = childTypeOf(location.type);
    const canHaveChildren = childType !== null;

    const {
        data: children,
        isFetching,
    } = useGetChildrenQuery(location.id, {
        skip: !expanded || !canHaveChildren,
    });

    const handleToggle = () => {
        if (!canHaveChildren) return;
        setExpanded((previous) => !previous);
    };

    return (
        <div
            className={[
                "location-explorer-node",
                expanded ? "location-explorer-node--expanded" : "",
                depth > 0 ? "location-explorer-node--nested" : "",
            ]
                .filter(Boolean)
                .join(" ")}
        >
            <div className="location-explorer-node__row">
                <div className="location-explorer-node__hierarchy">
                    <button
                        type="button"
                        className="location-explorer-node__toggle"
                        onClick={handleToggle}
                        disabled={!canHaveChildren}
                        aria-label={
                            canHaveChildren
                                ? expanded
                                    ? `Collapse ${location.name} `
                                    : `Expand ${location.name} `
                                : `${location.name} has no child locations`
                        }
                        aria-expanded={
                            canHaveChildren ? expanded : undefined
                        }
                    >
                        {canHaveChildren ? (
                            expanded ? (
                                <ChevronDown
                                    size={16}
                                    aria-hidden="true"
                                />
                            ) : (
                                <ChevronRight
                                    size={16}
                                    aria-hidden="true"
                                />
                            )
                        ) : (
                            <span
                                className="location-explorer-node__leaf"
                                aria-hidden="true"
                            />
                        )}
                    </button>

                    <div className="location-explorer-node__identity">
                        <span className="location-explorer-node__name">
                            {location.name}
                        </span>

                        {depth === 0 && (
                            <span className="location-explorer-node__root-label">
                                Root location
                            </span>
                        )}
                    </div>
                </div>

                <div className="location-explorer-node__type">
                    <span className="location-explorer-node__badge">
                        {LOCATION_TYPE_LABELS[location.type]}
                    </span>
                </div>

                <div className="location-explorer-node__code">
                    {location.code ? (
                        <span className="location-explorer-node__code-value">
                            {location.code}
                        </span>
                    ) : (
                        <span className="location-explorer-node__code-empty">
                            —
                        </span>
                    )}
                </div>

                <div className="location-explorer-node__actions">
                    {canHaveChildren && (
                        <button
                            type="button"
                            className="location-explorer-node__add"
                            onClick={() => onAddChild(location)}
                            aria-label={`Add ${LOCATION_TYPE_LABELS[childType]
                                } under ${location.name} `}
                            title={`Add ${LOCATION_TYPE_LABELS[childType]
                                } `}
                        >
                            <Plus size={16} aria-hidden="true" />
                        </button>
                    )}
                </div>
            </div>

            {expanded && (
                <div className="location-explorer-node__children">
                    {isFetching && (
                        <div className="location-explorer-node__loading">
                            <Loader2
                                size={15}
                                className="location-explorer-node__spinner"
                                aria-hidden="true"
                            />

                            <span>
                                Loading{" "}
                                {LOCATION_TYPE_LABELS[childType!].toLowerCase()}
                                ...
                            </span>
                        </div>
                    )}

                    {!isFetching && children?.length === 0 && (
                        <div className="location-explorer-node__empty">
                            <span>
                                No{" "}
                                {LOCATION_TYPE_LABELS[
                                    childType!
                                ].toLowerCase()}{" "}
                                records yet.
                            </span>

                            <button
                                type="button"
                                className="location-explorer-node__empty-action"
                                onClick={() => onAddChild(location)}
                            >
                                Add{" "}
                                {LOCATION_TYPE_LABELS[
                                    childType!
                                ].toLowerCase()}
                            </button>
                        </div>
                    )}

                    {!isFetching &&
                        children?.map((child) => (
                            <LocationExplorerNode
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

export default LocationExplorerNode;