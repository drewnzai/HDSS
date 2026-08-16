import { useEffect } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { useDeleteUserMutation } from "../../store/AdminUserApi";
import type { UserSummary } from "../../models/UserSummary";

interface LocationState {
    user?: UserSummary;
}

function UserDelete() {
    const location = useLocation();
    const navigate = useNavigate();
    const [deleteUser, { isLoading, error }] = useDeleteUserMutation();

    const user = (location.state as LocationState | null)?.user;

    // Guard against direct navigation or a page refresh, where state is lost
    // and there's no endpoint to look a single user up by username.
    useEffect(() => {
        if (!user) {
            navigate("/admin/users", { replace: true });
        }
    }, [user, navigate]);

    if (!user) {
        return null;
    }

    const handleDelete = async () => {
        try {
            await deleteUser(user).unwrap();
            navigate("/admin/users", { replace: true });
        } catch {
            // error state below reflects the failure
        }
    };

    return (
        <div className="card">
            <span className="ledger-section__eyebrow">§ Admin — Confirm Deletion</span>
            <h1>Delete user</h1>
            <p>
                This will remove <span className="record-id">{user.username}</span> (
                {user.firstName} {user.lastName}, {user.email}) from the system. This action can't
                be undone from here.
            </p>

            {error && <p className="field__error">Couldn't delete this user. Try again.</p>}

            <div className="field-group" style={{ flexDirection: "row", gap: "var(--space-3)" }}>
                <button className="btn btn--danger" onClick={handleDelete} disabled={isLoading}>
                    {isLoading ? "Deleting…" : "Confirm delete"}
                </button>
                <button className="btn btn--ghost" onClick={() => navigate("/admin/users")}>
                    Cancel
                </button>
            </div>
        </div>
    );
}

export default UserDelete;