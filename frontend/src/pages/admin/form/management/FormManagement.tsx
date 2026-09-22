import { useState } from "react";
import DataTable, { type DataTableColumn } from "../../../../components/data/DataTable";
import DataTablePagination from "../../../../components/data/DataTablePagination";
import PageContainer from "../../../../components/PageContainer";
import type { FormDto } from "../../../../models/FormDto";
import { useGetAllFormsQuery } from "../../../../store/FormApi";

function FormManagement() {
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(10)

    const {
        data,
        isLoading,
        isFetching,
        error,
        refetch,
    } = useGetAllFormsQuery({
        page,
        size: pageSize,
    });

    const forms = data?.data ?? [];

    const columns: DataTableColumn<FormDto>[] = [
        {
            key: "name",
            header: "Name",
            render: (form) => (
                <span className="form-management__name">
                    {form.name}
                </span>
            ),
        },
        {
            key: "title",
            header: "Title",
            render: (form) => form.title,
        },
        {
            key: "category",
            header: "Category",
            render: (form) => form.category,
        },
        {
            key: "target",
            header: "Target",
            render: (form) => form.target,
        },
        {
            key: "version",
            header: "Version",
            render: (form) => `v${form.version} `,
        },
        {
            key: "status",
            header: "Status",
            render: (form) => {
                if (form.status === "DRAFT") {
                    return (
                        <span className="status-badge status-badge--warning">
                            Draft
                        </span>
                    );
                } else {
                    return (
                        <span className="status-badge status-badge--success">
                            Published
                        </span>
                    );
                }
            },
        },
        {
            key: "active",
            header: "Active",
            render: (form) => {
                if (form.active) {
                    return (
                        <span className="status-badge status-badge--success">
                            Active
                        </span>
                    );
                }

                return (
                    <span className="status-badge status-badge--muted">
                        Inactive
                    </span>
                );
            }
        }
    ];

    return (
        <PageContainer size="wide">
            <div className="page-header">
                <div>
                    <h1>Forms</h1>

                    <p>
                        Manage forms available in the portal.
                    </p>
                </div>

                <button type="button">
                    Create form
                </button>
            </div>

            {error ? (
                <div className="page-state">
                    <p>
                        Unable to load forms.
                    </p>

                    <button
                        type="button"
                        onClick={refetch}
                    >
                        Try again
                    </button>
                </div>
            ) : (
                <>
                    <DataTable<FormDto>
                        columns={columns}
                        data={forms}
                        getRowKey={(form) => form.id}
                        isLoading={isLoading || isFetching}
                        emptyMessage="No forms have been created yet."
                        loadingMessage="Loading forms"
                    />

                    <DataTablePagination
                        page={page}
                        pageSize={pageSize}
                        totalItems={data?.totalElements ?? 0}
                        onPageChange={setPage}
                        onPageSizeChange={setPageSize}
                    />
                </>
            )}
        </PageContainer>
    );
}

export default FormManagement;

