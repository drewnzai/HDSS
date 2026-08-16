import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import type { UserSummary } from "../../models/UserSummary";

interface LocationState {
    user?: UserSummary;
}

function UserDetail() {
    const location = useLocation();
    const navigate = useNavigate();

    const user = (location.state as LocationState | null)?.user;

    // Guard against direct navigation or a refresh, where state is lost
    // and there's no endpoint to look a single user up by username yet.
    useEffect(() => {
        if (!user) {
            navigate("/admin/users", { replace: true });
        }
    }, [user, navigate]);

    if (!user) {
        return null;
    }

    return (
        <div className="card">
            <span className="ledger-section__eyebrow">§ Admin — User Record</span>
            <h1 className="record-id">{user.username}</h1>

            <div className="ledger-section">
                <span className="ledger-section__eyebrow">§ 01 — Details</span>
                <div className="field-group">
                    <div className="field">
                        <span className="field__label">First name</span>
                        <p style={{ margin: 0 }}>{user.firstName}</p>
                    </div>
                    <div className="field">
                        <span className="field__label">Last name</span>
                        <p style={{ margin: 0 }}>{user.lastName}</p>
                    </div>
                    <div className="field">
                        <span className="field__label">Email</span>
                        <p style={{ margin: 0 }}>{user.email}</p>
                    </div>
                    <div className="field">
                        <span className="field__label">Status</span>
                        <p style={{ margin: 0 }}>
                            {user.deleted ? (
                                <span className="badge badge--danger">Deleted</span>
                            ) : user.enabled ? (
                                <span className="badge badge--success">Active</span>
                            ) : (
                                <span className="badge badge--warn">Disabled</span>
                            )}
                        </p>
                    </div>
                </div>
            </div>

            <button
                className="btn btn--ghost"
                onClick={() => navigate("/admin/users")}
                style={{ marginTop: "var(--space-5)" }}
            >
                Back to users
            </button>
        </div>
    );
}

export default UserDetail;