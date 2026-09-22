import { useState } from "react";
import DataTable, { type DataTableColumn } from "../../../../components/data/DataTable";
import DataTablePagination from "../../../../components/data/DataTablePagination";
import PageContainer from "../../../../components/PageContainer";
import type { FormDto } from "../../../../models/FormDto";
import { useGetAllFormsQuery } from "../../../../store/FormApi";

function FormManagement() {
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(10);

    const {
        data,
        isLoading,
        isFetching,
        isError,
        refetch,
    } = useGetAllFormsQuery({
        page,
        size,
    });

    const forms = data?.content ?? [];

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
            render: (form) => `v${ form.version } `,
        },
        {
            key: "status",
            header: "Status",
            render: (form) => {
                if (form.locked) {
                    return (
                        <span className="status-badge status-badge--warning">
                            Locked
                        </span>
                    );
                }

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
            },
        },
    ];

    const handlePageChange = (nextPage: number) => {
        setPage(nextPage);
    };

    const handlePageSizeChange = (nextSize: number) => {
        setSize(nextSize);
        setPage(0);
    };

    return (
        <PageContainer>
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

            {isError ? (
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
                        rowKey={(form) => form.id}
                        loading={isLoading}
                        emptyMessage="No forms have been created yet."
                    />

                    <DataTablePagination
                        page={data?.page ?? page}
                        size={data?.size ?? size}
                        totalElements={data?.totalElements ?? 0}
                        totalPages={data?.totalPages ?? 0}
                        onPageChange={handlePageChange}
                        onPageSizeChange={handlePageSizeChange}
                        loading={isFetching}
                    />
                </>
            )}
        </PageContainer>
    );
}

export default FormManagement;

