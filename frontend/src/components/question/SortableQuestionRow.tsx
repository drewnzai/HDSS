import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { Edit2, GripVertical, Trash2 } from "lucide-react";
import { Link } from "react-router-dom";
import type { QuestionDto } from "../../models/QuestionDto";

interface SortableQuestionRowProps {
    question: QuestionDto;
    formId: string;
    onDelete: (question: QuestionDto) => void;
    isDeleting: boolean;
}

export function SortableQuestionRow({
    question,
    formId,
    onDelete,
    isDeleting,
}: SortableQuestionRowProps) {
    const {
        attributes,
        listeners,
        setNodeRef,
        transform,
        transition,
        isDragging,
    } = useSortable({
        id: question.id,
    });

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
    };

    return (
        <tr
            ref={setNodeRef}
            style={style}
            className={
                isDragging
                    ? "question-management__row question-management__row--dragging"
                    : "question-management__row"
            }
        >
            <td className="question-management__drag-cell">
                <button
                    type="button"
                    className="question-management__drag-handle"
                    aria-label={`Reorder ${question.label}`}
                    {...attributes}
                    {...listeners}
                >
                    <GripVertical
                        size={18}
                        aria-hidden="true"
                    />
                </button>
            </td>

            <td>
                <span className="question-management__name">
                    {question.name}
                </span>
            </td>

            <td>{question.label}</td>

            <td>
                <span className="question-management__type">
                    {question.type}
                </span>
            </td>

            <td>
                {question.required ? (
                    <span className="status-badge status-badge--success">
                        Required
                    </span>
                ) : (
                    <span className="status-badge status-badge--muted">
                        Optional
                    </span>
                )}
            </td>

            <td>
                {question.choiceListName || "—"}
            </td>

            <td>
                {question.mappedEntity === "NONE" ? (
                    <span className="status-badge status-badge--muted">
                        Not mapped
                    </span>
                ) : (
                    <span className="question-management__mapped">
                        {question.mappedField
                            .split(",")
                            .map((field) => field.trim())
                            .filter(Boolean)
                            .map(
                                (field) =>
                                    `${question.mappedEntity}.${field}`
                            )
                            .join(", ")}
                    </span>
                )}
            </td>

            <td>
                <div className="question-management__actions">
                    <Link
                        to={`/admin/forms/${formId}/questions/${question.id}/edit`}
                        className="question-management__edit"
                        aria-label={`Edit ${question.label}`}
                    >
                        <Edit2 size={17} aria-hidden="true" />
                    </Link>

                    <button
                        type="button"
                        className="question-management__delete"
                        onClick={() => onDelete(question)}
                        disabled={isDeleting}
                        aria-label={`Delete ${question.label}`}
                    >
                        <Trash2 size={17} aria-hidden="true" />
                    </button>
                </div>
            </td>
        </tr>
    );
}