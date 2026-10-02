import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import DataTable, {
    type DataTableColumn,
} from "../../../../components/data/DataTable";
import DataTablePagination from "../../../../components/data/DataTablePagination";
import PageContainer from "../../../../components/PageContainer";
import type { FormDto } from "../../../../models/FormDto";
import { useGetAllFormsQuery, usePublishFormMutation } from "../../../../redux/FormApi";

import Flash from "../../../../components/flash/Flash";
import "./form-management.css";

function FormManagement() {
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const navigate = useNavigate();

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

    const [publishForm, { isLoading: isPublishing }] = usePublishFormMutation();
    const [publishingId, setPublishingId] = useState<number | null>(null);
    const [publishError, setPublishError] = useState<string | null>(null);

    const forms = data?.data ?? [];

    const handlePublish = async (form: FormDto) => {
        setPublishError(null);
        setPublishingId(form.id);

        try {
            await publishForm(form.id).unwrap();
        } catch {
            setPublishError(
                `Unable to publish "${form.title}". Please try again.`
            );
        } finally {
            setPublishingId(null);
        }
    };

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
            render: (form) => (
                <span className="form-management__category">
                    {form.category}
                </span>
            ),
        },
        {
            key: "target",
            header: "Target",
            render: (form) => form.target,
        },
        {
            key: "version",
            header: "Version",
            render: (form) => (
                <span className="form-management__version">
                    v{form.version}
                </span>
            ),
        },
        {
            key: "status",
            header: "Status",
            render: (form) => {
                if (form.status === "DRAFT") {
                    const isThisRowPublishing = isPublishing && publishingId === form.id;

                    return (
                        <div className="form-management__status-cell">
                            <span className="status-badge status-badge--warning">
                                Draft
                            </span>

                            <button
                                type="button"
                                className="form-management__publish"
                                disabled={isPublishing}
                                onClick={(event) => {
                                    // Row itself navigates on click — stop
                                    // that from also firing here.
                                    event.stopPropagation();
                                    handlePublish(form);
                                }}
                            >
                                {isThisRowPublishing ? "Publishing…" : "Publish"}
                            </button>
                        </div>
                    );
                }

                return (
                    <span className="status-badge status-badge--success">
                        Published
                    </span>
                );
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
            },
        },
    ];

    return (
        <PageContainer size="wide">
            <div className="form-management">
                <header className="form-management__header">
                    <div className="form-management__heading">
                        <span className="form-management__eyebrow">
                            Administration
                        </span>

                        <h1 className="form-management__title">
                            Forms
                        </h1>

                        <p className="form-management__description">
                            Manage forms available in the portal.
                        </p>
                    </div>

                    <Link
                        to="/admin/forms/create"
                        className="form-management__create"
                    >
                        Create form
                    </Link>
                </header>

                <Flash />

                {publishError && (
                    <div className="form-management__publish-error">
                        {publishError}
                    </div>
                )}

                {error ? (
                    <div className="form-management__state">
                        <div>
                            <h2>
                                Unable to load forms
                            </h2>

                            <p>
                                Something went wrong while retrieving
                                the forms.
                            </p>
                        </div>

                        <button
                            type="button"
                            className="form-management__retry"
                            onClick={refetch}
                        >
                            Try again
                        </button>
                    </div>
                ) : (
                    <section className="form-management__table-section">
                        <DataTable<FormDto>
                            columns={columns}
                            data={forms}
                            getRowKey={(form) => form.id}
                            isLoading={isLoading || isFetching}
                            emptyMessage="No forms have been created yet."
                            loadingMessage="Loading forms"
                            onRowClick={(form) => {
                                navigate(`/admin/forms/${form.id}/questions `, {
                                    state: { form },
                                });
                            }}
                        />

                        <div className="form-management__pagination">
                            <DataTablePagination
                                page={page}
                                pageSize={pageSize}
                                totalItems={data?.totalElements ?? 0}
                                onPageChange={setPage}
                                onPageSizeChange={(size) => {
                                    setPageSize(size);
                                    setPage(0);
                                }}
                            />
                        </div>
                    </section>
                )}
            </div>
        </PageContainer>
    );
}

export default FormManagement;