export type RoundStatus = "CREATING" | "COMPLETED" | "FAILED";

export interface DeltaSummary {
    individuals: number;
    households: number;
    memberships: number;
    visits: number;
    formResponses: number;
    answers: number;

    skippedVisits: number;
    skippedFormResponses: number;
    skippedAnswers: number;

    publishedForms: number;
    questionsInPublishedForms: number;

    movable: number;
}

export interface ValidationReport {
    passed: boolean;
    today: string;
    checksRun: number;
    delta: DeltaSummary;
    failures: string[];
}

export interface CreateRoundRequest {
    roundName: string;
}

export interface RoundDto {
    id: number;
    roundName: string;
    databaseName: string;
    status: RoundStatus;
    validationPassed: boolean;
    startedAt: string | null;
    completedAt: string | null;
    errorMessage: string | null;
    createdAt: string;
}

export interface RoundDetailsDto extends RoundDto {
    validationReport: ValidationReport | null;
}

export const getMovableCount = (delta: DeltaSummary): number =>
    delta.individuals +
    delta.households +
    delta.memberships +
    delta.visits +
    delta.formResponses +
    delta.answers;