import { useEffect } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { useDeleteUserMutation, useGetUserByUsernameQuery } from "../../store/AdminUserApi";
import type { UserSummary } from "../../models/UserSummary";

interface LocationState {
    user?: UserSummary;
}

function UserDelete() {
    const { username } = useParams<{ username: string }>();
    const location = useLocation();
    const navigate = useNavigate();
    const [deleteUser, { isLoading: isDeleting, error: deleteError }] = useDeleteUserMutation();

    const stateUser = (location.state as LocationState | null)?.user;

    const {
        data: fetchedUser,
        isLoading: isLoadingUser,
        error: fetchError
    } = useGetUserByUsernameQuery(username!, {
        skip: !username || Boolean(stateUser)
    });

    const user = stateUser ?? fetchedUser;

    useEffect(() => {
        if (!stateUser && !isLoadingUser && (fetchError || !fetchedUser)) {
            navigate("/admin/users", { replace: true });
        }
    }, [stateUser, isLoadingUser, fetchError, fetchedUser, navigate]);

    if (!user) {
        return isLoadingUser ? (
            <div className="card">
                <p>Loading user…</p>
            </div>
        ) : null;
    }

    const handleDelete = async () => {
        try {
            const message = await deleteUser(user).unwrap();
            navigate("/admin/users", { replace: true, state: { flash: message } });
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

            {deleteError && <p className="field__error">Couldn't delete this user. Try again.</p>}

            <div className="field-group" style={{ flexDirection: "row", gap: "var(--space-3)" }}>
                <button className="btn btn--danger" onClick={handleDelete} disabled={isDeleting}>
                    {isDeleting ? "Deleting…" : "Confirm delete"}
                </button>
                <button className="btn btn--ghost" onClick={() => navigate("/admin/users")}>
                    Cancel
                </button>
            </div>
        </div>
    );
}

export default UserDelete;