import { useNavigate, useParams } from "react-router-dom";
import { useDeleteUserMutation } from "../../store/AdminUserApi";

function UserDelete() {
    const { username } = useParams<{ username: string }>();
    const navigate = useNavigate();
    const [deleteUser, { isLoading, error }] = useDeleteUserMutation();

    const handleDelete = async () => {
        if (!username) return;
        try {
            await deleteUser(username).unwrap();
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
                This will remove <span className="record-id">{username}</span> from the system. This
                action can't be undone from here.
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