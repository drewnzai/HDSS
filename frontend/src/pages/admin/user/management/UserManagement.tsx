import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Eye, Trash2 } from "lucide-react";
import ConfirmDialog from "../../../../components/confirm-dialog/ConfirmDialog";
import DataTable, { type DataTableColumn } from "../../../../components/data/DataTable";
import DataTablePagination from "../../../../components/data/DataTablePagination";
import PageContainer from "../../../../components/PageContainer";
import type { UserSummary } from "../../../../models/UserSummary";
import { useDeleteUserMutation, useGetUsersQuery } from "../../../../redux/UserApi";
import "./user-management.css";
import type { LocationState } from "../../../LocationState";

function UserManagement() {
    const navigate = useNavigate();
    const location = useLocation();

    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);

    const [userToDelete, setUserToDelete] =
        useState<UserSummary | null>(null);

    const [flash, setFlash] = useState<string | null>(
        (location.state as LocationState | null)?.flash ?? null
    );

    const [deleteUser, { isLoading: isDeleting, error: deleteError }] =
        useDeleteUserMutation();

    useEffect(() => {
        if (!flash) return;

        const timer = setTimeout(() => setFlash(null), 4000);

        navigate(location.pathname, {
            replace: true,
            state: {},
        });

        return () => clearTimeout(timer);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [flash]);

    const { data, isLoading, isFetching, error } = useGetUsersQuery({
        page,
        size: pageSize,
    });

    const users = data?.data ?? [];
    const totalUsers = data?.totalElements ?? 0;

    const handleDelete = async () => {
        if (!userToDelete) return;

        try {
            const message = await deleteUser(userToDelete).unwrap();

            setUserToDelete(null);
            setFlash(message);
        } catch {
            // deleteError below reflects the failure.
        }
    };

    const columns: DataTableColumn<UserSummary>[] = [
        {
            key: "username",
            header: "Username",
            render: (user) => (
                <span className="user-management__username">
                    {user.username}
                </span>
            ),
        },
        {
            key: "name",
            header: "Name",
            render: (user) => (
                <span>
                    {user.firstName} {user.lastName}
                </span>
            ),
        },
        {
            key: "role",
            header: "Role",
            render: (user) => (
                <span className="user-management__role">
                    {user.role}
                </span>
            ),
        },
        {
            key: "actions",
            header: "Actions",
            className: "data-table__actions",
            render: (user) => (
                <div className="user-management__actions">
                    <button
                        type="button"
                        className="user-management__action"
                        onClick={(event) => {
                            event.stopPropagation();

                            navigate(`/admin/users/${user.username} `, {
                                state: { user },
                            });
                        }}
                        aria-label={`View ${user.username} `}
                        title="View user"
                    >
                        <Eye size={17} aria-hidden="true" />
                    </button>

                    <button
                        type="button"
                        className="user-management__action user-management__action--danger"
                        onClick={(event) => {
                            event.stopPropagation();
                            setUserToDelete(user);
                        }}
                        aria-label={`Delete ${user.username} `}
                        title="Delete user"
                    >
                        <Trash2 size={17} aria-hidden="true" />
                    </button>
                </div>
            ),
        },
    ];

    return (
        <>
            <PageContainer size="wide">
                <div className="user-management">
                    <div className="user-management__header">
                        <div>
                            <span className="user-management__eyebrow">
                                User Management
                            </span>

                            <h1 className="user-management__title">
                                Users
                            </h1>

                            <p className="user-management__description">
                                Manage user accounts and access to the HDSS
                                field portal.
                            </p>
                        </div>

                        <Link
                            to="/admin/users/create"
                            className="user-management__create"
                        >
                            Create user
                        </Link>
                    </div>

                    {flash && (
                        <div
                            className="flash flash--success"
                            role="status"
                            aria-live="polite"
                        >
                            {flash}
                        </div>
                    )}

                    {deleteError && (
                        <div
                            className="user-management__error"
                            role="alert"
                            aria-live="assertive"
                        >
                            Couldn't delete this user. Try again.
                        </div>
                    )}

                    {error && (
                        <div
                            className="user-management__error"
                            role="alert"
                            aria-live="assertive"
                        >
                            Couldn't load users. Try refreshing.
                        </div>
                    )}

                    <section className="user-management__table">
                        <DataTable
                            columns={columns}
                            data={users}
                            getRowKey={(user) => user.username}
                            isLoading={isLoading || isFetching}
                            emptyMessage="No users found."
                            loadingMessage="Loading users..."
                            onRowClick={(user) => {
                                navigate(`/admin/users/${user.username} `, {
                                    state: { user },
                                });
                            }}
                        />

                        <DataTablePagination
                            page={page}
                            pageSize={pageSize}
                            totalItems={totalUsers}
                            onPageChange={setPage}
                            onPageSizeChange={setPageSize}
                        />
                    </section>
                </div>
            </PageContainer>

            <ConfirmDialog
                open={Boolean(userToDelete)}
                title="Delete user?"
                description={
                    userToDelete ? (
                        <>
                            This will remove{" "}
                            <strong>{userToDelete.username}</strong>{" "}
                            ({userToDelete.firstName}{" "}
                            {userToDelete.lastName},{" "}
                            {userToDelete.email}) from the system.
                            This action can't be undone from here.
                        </>
                    ) : null
                }
                confirmLabel="Confirm delete"
                confirmLoadingLabel="Deleting…"
                cancelLabel="Cancel"
                isLoading={isDeleting}
                onConfirm={handleDelete}
                onCancel={() => {
                    if (!isDeleting) {
                        setUserToDelete(null);
                    }
                }}
            />
        </>
    );
}

export default UserManagement;