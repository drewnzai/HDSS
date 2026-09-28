import { type DragEndEvent, DndContext, closestCenter } from "@dnd-kit/core";
import { SortableContext, verticalListSortingStrategy } from "@dnd-kit/sortable";
import type { QuestionDto } from "../../models/QuestionDto";
import { SortableQuestionRow } from "./SortableQuestionRow";

interface SortableQuestionTableProps {
    questions: QuestionDto[];
    formId: string;
    onReorder: (event: DragEndEvent) => void;
    onDelete: (question: QuestionDto) => void;
    isDeleting: boolean;
}

export function SortableQuestionTable({
    questions,
    formId,
    onReorder,
    onDelete,
    isDeleting,
}: SortableQuestionTableProps) {
    return (
        <DndContext
            collisionDetection={closestCenter}
            onDragEnd={onReorder}
        >
            <table className="question-management__table">
                <thead>
                    <tr>
                        <th aria-label="Reorder" />
                        <th>Name</th>
                        <th>Label</th>
                        <th>Type</th>
                        <th>Required</th>
                        <th>Choice list</th>
                        <th>Maps to</th>
                        <th aria-label="Actions" />
                    </tr>
                </thead>

                <SortableContext
                    items={questions.map((question) => question.id)}
                    strategy={verticalListSortingStrategy}
                >
                    <tbody>
                        {questions.map((question) => (
                            <SortableQuestionRow
                                key={question.id}
                                question={question}
                                formId={formId}
                                onDelete={onDelete}
                                isDeleting={isDeleting}
                            />
                        ))}
                    </tbody>
                </SortableContext>
            </table>
        </DndContext>
    );
}