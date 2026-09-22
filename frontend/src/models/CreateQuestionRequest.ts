import type { MappedEntity } from "./types/MappedEntity";
import type { QuestionType } from "./types/QuestionTypes";

export interface CreateQuestionRequest{
    name: string;
    label: string;
    hint: string;
    type: QuestionType;
    required: boolean;
    relevant: string;
    constraint: string;
    constraintMessage: string;
    calculation: string;
    choiceListName: string;
    mappedEntity: MappedEntity;
    mappedField: string;
}