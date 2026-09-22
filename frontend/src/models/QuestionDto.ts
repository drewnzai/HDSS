import type { MappedEntity } from "./types/MappedEntity";
import type { QuestionType } from "./types/QuestionTypes";

export interface QuestionDto{
    id: number;
    formId: number;
    name: string;
    label: string;
    hint: string;
    type: QuestionType;
    required: boolean;
    relevent: string;
    constraint: string;
    constraintMessage: string;
    calculation: string;
    choiceListName: string;
    orderIndex: string;
    mappedEntity: MappedEntity;
    mappedField: string;
}