import { useParams, Link, useLocation, useNavigate } from "react-router-dom";
import DataTable, { type DataTableColumn } from "../../../../components/data/DataTable";
import PageContainer from "../../../../components/PageContainer";
import type { QuestionDto } from "../../../../models/QuestionDto";
import { useGetFormByIdQuery } from "../../../../redux/FormApi";
import { useGetQuestionsByFormQuery, useDeleteQuestionMutation } from "../../../../redux/QuestionApi";
import { Edit2, Trash2 } from "lucide-react";
import "./question-management.css"
import { useEffect, useState } from "react";
import type { LocationState } from "../../../LocationState";

function QuestionManagement() {
    const { formId } = useParams<{ formId: string }>();
    const numericFormId = Number(formId);

    const navigate = useNavigate();
    const location = useLocation();
    const [flash, setFlash] = useState<string | null>(
        (location.state as LocationState | null)?.flash ?? null
    );

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

    const {
        data: form,
        isLoading: isFormLoading,
    } = useGetFormByIdQuery(numericFormId, {
        skip: Number.isNaN(numericFormId),
    });

    const {
        data: questions,
        isLoading,
        isFetching,
        error,
        refetch,
    } = useGetQuestionsByFormQuery(numericFormId, {
        skip: Number.isNaN(numericFormId),
    });

    const [deleteQuestion, { isLoading: isDeleting }] =
        useDeleteQuestionMutation();

    const handleDelete = async (question: QuestionDto) => {
        const confirmed = window.confirm(
            `Delete question "${question.label}"? This cannot be undone.`
        );

        if (!confirmed) {
            return;
        }

        try {
            await deleteQuestion({
                formId: numericFormId,
                questionId: question.id,
            }).unwrap();
        } catch {
            // API error surfaced via the error state below on next fetch.
        }
    };

    const columns: DataTableColumn<QuestionDto>[] = [
        {
            key: "name",
            header: "Name",
            render: (question) => (
                <span className="question-management__name">
                    {question.name}
                </span>
            ),
        },
        {
            key: "label",
            header: "Label",
            render: (question) => question.label,
        },
        {
            key: "type",
            header: "Type",
            render: (question) => (
                <span className="question-management__type">
                    {question.type}
                </span>
            ),
        },
        {
            key: "required",
            header: "Required",
            render: (question) => {
                if (question.required) {
                    return (
                        <span className="status-badge status-badge--success">
                            Required
                        </span>
                    );
                }

                return (
                    <span className="status-badge status-badge--muted">
                        Optional
                    </span>
                );
            },
        },
        {
            key: "choiceListName",
            header: "Choice list",
            render: (question) => question.choiceListName || "—",
        },
        {
            key: "mappedEntity",
            header: "Maps to",
            render: (question) => {
                if (question.mappedEntity === "NONE") {
                    return (
                        <span className="status-badge status-badge--muted">
                            Not mapped
                        </span>
                    );
                }

                // mappedField may be a comma-separated list of field
                // names (e.g. a GEOPOINT question mapping to
                // "latitude,longitude") — render each one qualified by
                // the entity, joined for readability.
                const fields = question.mappedField
                    .split(",")
                    .map((f) => f.trim())
                    .filter(Boolean);

                return (
                    <span className="question-management__mapped">
                        {fields
                            .map(
                                (field) =>
                                    `${question.mappedEntity}.${field}`
                            )
                            .join(", ")}
                    </span>
                );
            },
        },
        {
            key: "actions",
            header: "",
            render: (question) => (
                <div className="question-management__actions">
                    <Link
                        to={`/admin/forms/${formId}/questions/${question.id}/edit`}
                        className="question-management__edit"
                    >
                        <Edit2 size={17} aria-hidden="true" />
                    </Link>

                    <button
                        type="button"
                        className="question-management__delete"
                        onClick={() => handleDelete(question)}
                        disabled={isDeleting}
                    >
                        <Trash2 size={17} aria-hidden="true" />
                    </button>
                </div>
            ),
        },
    ];

    const questionList = questions ?? [];

    return (
        <PageContainer size="wide">
            <div className="question-management">
                <header className="question-management__header">
                    <div className="question-management__heading">
                        <span className="question-management__eyebrow">
                            Administration ·{" "}
                            <Link to="/admin/forms">Forms</Link>
                        </span>

                        <h1 className="question-management__title">
                            {isFormLoading
                                ? "Loading…"
                                : form?.title ?? "Questions"}
                        </h1>

                        <p className="question-management__description">
                            {form
                                ? `${form.category} · ${form.target} · v${form.version}`
                                : "Manage the questions in this form."}
                        </p>
                    </div>

                    <Link
                        to={`/admin/forms/${formId}/questions/create`}
                        className="question-management__create"
                    >
                        Add question
                    </Link>
                </header>

                {flash && (
                    <div
                        className="flash flash--success"
                        role="status"
                        aria-live="polite"
                    >
                        {flash}
                    </div>
                )}

                {error ? (
                    <div className="question-management__state">
                        <div>
                            <h2>Unable to load questions</h2>
                            <p>
                                Something went wrong while retrieving
                                the questions for this form.
                            </p>
                        </div>

                        <button
                            type="button"
                            className="question-management__retry"
                            onClick={refetch}
                        >
                            Try again
                        </button>
                    </div>
                ) : (
                    <section className="question-management__table-section">
                        <DataTable<QuestionDto>
                            columns={columns}
                            data={questionList}
                            getRowKey={(question) => question.id}
                            isLoading={isLoading || isFetching}
                            emptyMessage="No questions have been added to this form yet."
                            loadingMessage="Loading questions"
                        />
                    </section>
                )}
            </div>
        </PageContainer>
    );
}

export default QuestionManagement;