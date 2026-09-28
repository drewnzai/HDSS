import type { DragEndEvent } from "@dnd-kit/core";
import { arrayMove } from "@dnd-kit/sortable";
import { Save } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Flash from "../../../../components/flash/Flash";
import PageContainer from "../../../../components/PageContainer";
import { SortableQuestionTable } from "../../../../components/question/SortableQuestionTable";
import type { QuestionDto } from "../../../../models/QuestionDto";
import { useGetFormByIdQuery } from "../../../../redux/FormApi";
import { useDeleteQuestionMutation, useGetQuestionsByFormQuery, useReorderQuestionsMutation } from "../../../../redux/QuestionApi";
import "./question-management.css";

function QuestionManagement() {
    const { formId } = useParams<{ formId: string }>();
    const [orderedQuestions, setOrderedQuestions] = useState<QuestionDto[]>(
        []
    );
    const [orderDirty, setOrderDirty] = useState(false);

    const navigate = useNavigate();

    const numericFormId = Number(formId);

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

    const [
        reorderQuestions,
        { isLoading: isReordering },
    ] = useReorderQuestionsMutation();

    useEffect(() => {
        setOrderedQuestions(questions ?? []);
    }, [questions]);

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

            navigate(location.pathname, {
                replace: true,
                state: {
                    flash: "Question deleted successfully.",
                    flashType: "success",
                },
            });
        } catch {
            // API error surfaced via the error state below on next fetch.
        }
    };

    const handleDragEnd = (event: DragEndEvent) => {
        const { active, over } = event;

        if (!over || active.id === over.id) {
            return;
        }

        setOrderedQuestions((current) => {
            const oldIndex = current.findIndex(
                (question) => question.id === active.id
            );

            const newIndex = current.findIndex(
                (question) => question.id === over.id
            );

            return arrayMove(current, oldIndex, newIndex);
        });

        setOrderDirty(true);
    };

    const handleSaveOrder = async () => {
        try {
            const reordered = await reorderQuestions({
                formId: numericFormId,
                body: {
                    questionIdsInOrder: orderedQuestions.map(
                        (question) => question.id
                    ),
                },
            }).unwrap();

            setOrderedQuestions(reordered);
            setOrderDirty(false);

            navigate(location.pathname, {
                replace: true,
                state: {
                    flash: "Question order saved successfully.",
                    flashType: "success",
                },
            });
        } catch {
            navigate(location.pathname, {
                replace: true,
                state: {
                    flash: "Unable to save the question order. Please try again.",
                    flashType: "error",
                },
            });
        }
    };

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

                    <div className="question-management__header-actions">
                        {orderDirty && (
                            <button
                                type="button"
                                className="question-management__save-order"
                                onClick={handleSaveOrder}
                                disabled={isReordering}
                            >
                                <Save size={17} aria-hidden="true" />

                                {isReordering
                                    ? "Saving…"
                                    : "Save order"}
                            </button>
                        )}

                        <Link
                            to={`/admin/forms/${formId}/questions/create`}
                            className="question-management__create"
                        >
                            Add question
                        </Link>
                    </div>
                </header>

                <Flash />

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
                        {isLoading || isFetching ? (
                            <div className="question-management__loading">
                                Loading questions…
                            </div>
                        ) : orderedQuestions.length === 0 ? (
                            <div className="question-management__empty">
                                No questions have been added to this form yet.
                            </div>
                        ) : (
                            <SortableQuestionTable
                                questions={orderedQuestions}
                                formId={formId!}
                                onReorder={handleDragEnd}
                                onDelete={handleDelete}
                                isDeleting={isDeleting}
                            />
                        )}
                    </section>
                )}
            </div>
        </PageContainer>
    );
}

export default QuestionManagement;