import { useLocation, useNavigate, useParams } from "react-router-dom";
import { useGetUserByUsernameQuery } from "../../store/AdminUserApi";
import type { UserSummary } from "../../models/UserSummary";

interface LocationState {
    user?: UserSummary;
}

function UserDetail() {
    const { username } = useParams<{ username: string }>();
    const location = useLocation();
    const navigate = useNavigate();

    const stateUser = (location.state as LocationState | null)?.user;

    // Only hit the network if we didn't already get the user via navigation state
    // (e.g. a refresh, a bookmarked link, or someone pasting the URL directly).
    const {
        data: fetchedUser,
        isLoading,
        error
    } = useGetUserByUsernameQuery(username!, {
        skip: !username || Boolean(stateUser)
    });

    const user = stateUser ?? fetchedUser;

    if (!stateUser && isLoading) {
        return (
            <div className="card">
                <p>Loading user…</p>
            </div>
        );
    }

    if (!user || error) {
        return (
            <div className="card">
                <p className="field__error">Couldn't find that user.</p>
                <button className="btn btn--ghost" onClick={() => navigate("/admin/users")}>
                    Back to users
                </button>
            </div>
        );
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