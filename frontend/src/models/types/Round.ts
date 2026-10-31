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
}

export interface Violation {
    check: string;
    ref: string;
    detail: string;
}

export interface CheckFailure {
    id: string;
    description: string;
    count: number;
    countIsLowerBound: boolean;
    samples: Violation[];
}

export interface ValidationReport {
    passed: boolean;
    checkedOn: string;
    checksRun: number;
    delta: DeltaSummary;
    failures: CheckFailure[];
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