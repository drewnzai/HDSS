import { useState } from "react";
import type { FormEvent } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useCreateLocationMutation } from "../../store/LocationApi";
import { childTypeOf, LOCATION_TYPE_LABELS } from "../../models/Location";
import type { LocationDto } from "../../models/Location";

interface NavState {
    parent: LocationDto | null;
}

function CreateLocation() {
    const location = useLocation();
    const navigate = useNavigate();
    const [createLocation, { isLoading, error }] = useCreateLocationMutation();

    const parent = (location.state as NavState | null)?.parent ?? null;
    const type = parent ? childTypeOf(parent.type) : "COUNTRY";

    const [name, setName] = useState("");

    // A parent that can't have children shouldn't have reached this page at all.
    if (parent && !type) {
        return (
            <div className="card">
                <p className="field__error">
                    {LOCATION_TYPE_LABELS[parent.type]} is the deepest level — it can't have children.
                </p>
                <button className="btn btn--ghost" onClick={() => navigate("/admin/locations")}>
                    Back to locations
                </button>
            </div>
        );
    }

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        try {
            await createLocation({
                name,
                type: type!,
                parentId: parent ? parent.id : null
            }).unwrap();

            navigate("/admin/locations", {
                replace: true,
                state: { flash: `${LOCATION_TYPE_LABELS[type!]} created successfully.` }
            });
        } catch {
            // error state below reflects the failure
        }
    };

    return (
        <div className="card">
            <span className="ledger-section__eyebrow">§ Admin — Add Location</span>
            <h1>Add {LOCATION_TYPE_LABELS[type!]}</h1>
            {parent && (
                <p>
                    Under: <span className="record-id">{parent.name}</span> ({LOCATION_TYPE_LABELS[parent.type]})
                </p>
            )}

            <form onSubmit={handleSubmit} noValidate>
                <div className="ledger-section">
                    <span className="ledger-section__eyebrow">§ 01 — Details</span>
                    <div className="field-group">
                        <div className="field">
                            <label className="field__label" htmlFor="name">
                                Name
                            </label>
                            <input id="name" value={name} onChange={(e) => setName(e.target.value)} required />
                        </div>
                    </div>
                </div>

                {error && (
                    <p className="field__error" style={{ marginTop: "var(--space-4)" }}>
                        Couldn't create this location. It may already exist under this parent.
                    </p>
                )}

                <div
                    className="field-group"
                    style={{ flexDirection: "row", gap: "var(--space-3)", marginTop: "var(--space-6)" }}
                >
                    <button type="submit" className="btn btn--primary" disabled={isLoading}>
                        {isLoading ? "Creating…" : "Create"}
                    </button>
                    <button type="button" className="btn btn--ghost" onClick={() => navigate("/admin/locations")}>
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    );
}

export default CreateLocation;