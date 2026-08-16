import { useState, useEffect} from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useGetUsersQuery } from "../../store/AdminUserApi";

const PAGE_SIZE_OPTIONS = [10, 25, 50];

interface LocationState {
    flash?: string;
}

function UserManagement() {
    const [page, setPage] = useState(0); // backend is 0-indexed
    const [size, setSize] = useState(10);
    const navigate = useNavigate();
    const location = useLocation();

    const [flash, setFlash] = useState<string | null>(
        (location.state as LocationState | null)?.flash ?? null
    );


    const { data, isLoading, isFetching, error } = useGetUsersQuery({ page, size });

    useEffect(() => {
        if (!flash) return;
        const timer = setTimeout(() => setFlash(null), 4000);
        navigate(location.pathname, { replace: true, state: {} });
        return () => clearTimeout(timer);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [flash]);

    const handleSizeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setSize(Number(e.target.value));
        setPage(0); // reset to first page when page size changes
    };

    const canGoPrev = page > 0;
    const canGoNext = data ? page < data.totalPages - 1 : false;

    return (
        <div className="card card--wide">
            {flash && (
                <div className="flash flash--success" role="status">
                    {flash}
                </div>
            )}
            <span className="ledger-section__eyebrow">§ Admin — User Management</span>
            <h1>Users</h1>



            {isLoading && <p>Loading users…</p>}
            {error && <p className="field__error">Couldn't load users. Try refreshing.</p>}

            {data && (
                <>
                    <div className="table-wrap">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Username</th>
                                    <th>First name</th>
                                    <th>Last name</th>
                                    <th>Email</th>
                                    <th>Status</th>
                                    <th className="data-table__actions-col">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data.data.map((user) => (
                                    <tr key={user.username}>
                                        <td className="record-id">{user.username}</td>
                                        <td>{user.firstName}</td>
                                        <td>{user.lastName}</td>
                                        <td>{user.email}</td>
                                        <td>
                                            {user.deleted ? (
                                                <span className="badge badge--danger">Deleted</span>
                                            ) : user.enabled ? (
                                                <span className="badge badge--success">Active</span>
                                            ) : (
                                                <span className="badge badge--warn">Disabled</span>
                                            )}
                                        </td>
                                        <td className="data-table__actions">
                                            <button
                                                className="btn btn--ghost btn--sm"
                                                onClick={() => navigate(`/admin/users/${user.username}`, { state: { user } })}
                                            >
                                                Display
                                            </button>
                                            <button
                                                className="btn btn--danger-ghost btn--sm"
                                                onClick={() =>
                                                    navigate(`/admin/users/${user.username}/delete`, { state: { user } })
                                                }
                                            >
                                                Delete
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                {data.data.length === 0 && (
                                    <tr>
                                        <td colSpan={5} className="data-table__empty">
                                            No users found.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>

                    <div className="pagination">
                        <div className="pagination__size">
                            <label htmlFor="pageSize">Rows per page</label>
                            <select id="pageSize" value={size} onChange={handleSizeChange}>
                                {PAGE_SIZE_OPTIONS.map((opt) => (
                                    <option key={opt} value={opt}>
                                        {opt}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="pagination__controls">
                            <span className="pagination__summary">
                                Page {data.totalPages === 0 ? 0 : page + 1} of {data.totalPages}
                                {" · "}
                                {data.totalElements} total
                            </span>
                            <button
                                className="btn btn--ghost btn--sm"
                                onClick={() => setPage((p) => p - 1)}
                                disabled={!canGoPrev || isFetching}
                            >
                                Previous
                            </button>
                            <button
                                className="btn btn--ghost btn--sm"
                                onClick={() => setPage((p) => p + 1)}
                                disabled={!canGoNext || isFetching}
                            >
                                Next
                            </button>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
}

export default UserManagement;