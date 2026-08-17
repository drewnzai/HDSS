import { DataGrid } from "@mui/x-data-grid";
import type { GridColDef, GridPaginationModel, GridRenderCellParams } from "@mui/x-data-grid";
import { ThemeProvider } from "@mui/material/styles";
import { useTheme } from "../theme/ThemeContext";
import { getMuiTheme } from "../theme/muiTheme";
import type { UserSummary } from "../models/UserSummary";

interface UsersDataGridProps {
    users: UserSummary[];
    rowCount: number;
    paginationModel: GridPaginationModel;
    onPaginationModelChange: (model: GridPaginationModel) => void;
    isLoading: boolean;
    onDisplay: (user: UserSummary) => void;
    onDelete: (user: UserSummary) => void;
}

function UsersDataGrid({
    users,
    rowCount,
    paginationModel,
    onPaginationModelChange,
    isLoading,
    onDisplay,
    onDelete
}: UsersDataGridProps) {
    const { theme } = useTheme();

    const columns: GridColDef<UserSummary>[] = [
        { field: "username", headerName: "Username", flex: 1, minWidth: 140 },
        { field: "firstName", headerName: "First name", flex: 1, minWidth: 120 },
        { field: "lastName", headerName: "Last name", flex: 1, minWidth: 120 },
        { field: "email", headerName: "Email", flex: 1.5, minWidth: 180 },
        {
            field: "status",
            headerName: "Status",
            minWidth: 110,
            sortable: false,
            renderCell: (params: GridRenderCellParams<UserSummary>) => {
                const user = params.row;
                if (user.deleted) return <span className="badge badge--danger">Deleted</span>;
                if (user.enabled) return <span className="badge badge--success">Active</span>;
                return <span className="badge badge--warn">Disabled</span>;
            }
        },
        {
            field: "actions",
            headerName: "Actions",
            minWidth: 200,
            sortable: false,
            filterable: false,
            renderCell: (params: GridRenderCellParams<UserSummary>) => {
                const user = params.row;
                return (
                    <div className="data-table__actions">
                        <button className="btn btn--ghost btn--sm btn--action" onClick={() => onDisplay(user)}>
                            Display
                        </button>
                        {!user.deleted ? (
                            <button
                                className="btn btn--danger-ghost btn--sm btn--action"
                                onClick={() => onDelete(user)}
                            >
                                Delete
                            </button>
                        ) : (
                            <span className="btn--action-spacer" aria-hidden="true" />
                        )}
                    </div>
                );
            }
        }
    ];

    return (
        <ThemeProvider theme={getMuiTheme(theme)}>
            <div style={{ width: "100%" }}>
                <DataGrid
                    rows={users}
                    columns={columns}
                    getRowId={(row) => row.username}
                    rowCount={rowCount}
                    loading={isLoading}
                    paginationMode="server"
                    paginationModel={paginationModel}
                    onPaginationModelChange={onPaginationModelChange}
                    pageSizeOptions={[10, 25, 50]}
                    disableRowSelectionOnClick
                    disableColumnMenu
                    autoHeight
                    sx={{
                        "& .MuiDataGrid-cell": {
                            display: "flex",
                            alignItems: "center"
                        }
                    }}
                />
            </div>
        </ThemeProvider>
    );
}

export default UsersDataGrid;