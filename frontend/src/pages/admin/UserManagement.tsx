import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import type { GridPaginationModel } from "@mui/x-data-grid";
import { useGetUsersQuery } from "../../store/AdminUserApi";
import UsersDataGrid from "../../components/UsersDataGrid";
import type { UserSummary } from "../../models/UserSummary";

interface LocationState {
    flash?: string;
}

function UserManagement() {
    const navigate = useNavigate();
    const location = useLocation();

    const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
        page: 0,
        pageSize: 10
    });

    const [flash, setFlash] = useState<string | null>(
        (location.state as LocationState | null)?.flash ?? null
    );

    useEffect(() => {
        if (!flash) return;
        const timer = setTimeout(() => setFlash(null), 4000);
        navigate(location.pathname, { replace: true, state: {} });
        return () => clearTimeout(timer);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [flash]);

    const { data, isLoading, isFetching, error } = useGetUsersQuery({
        page: paginationModel.page,
        size: paginationModel.pageSize
    });

    return (
        <div className="card card--wide">
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                <div>
                    <span className="ledger-section__eyebrow">§ Admin — User Management</span>
                    <h1>Users</h1>
                </div>
                <button className="btn btn--primary" onClick={() => navigate("/admin/users/create")}>
                    Create user
                </button>
            </div>

            {flash && (
                <div className="flash flash--success" role="status">
                    {flash}
                </div>
            )}

            {error && <p className="field__error">Couldn't load users. Try refreshing.</p>}

            <UsersDataGrid
                users={data?.data ?? []}
                rowCount={data?.totalElements ?? 0}
                paginationModel={paginationModel}
                onPaginationModelChange={setPaginationModel}
                isLoading={isLoading || isFetching}
                onDisplay={(user: UserSummary) =>
                    navigate(`/admin/users/${user.username}`, { state: { user } })
                }
                onDelete={(user: UserSummary) =>
                    navigate(`/admin/users/${user.username}/delete`, { state: { user } })
                }
            />
        </div>
    );
}

export default UserManagement;