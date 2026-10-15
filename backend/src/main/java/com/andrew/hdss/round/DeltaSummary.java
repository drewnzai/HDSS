package com.andrew.hdss.round;

public record DeltaSummary(
        long individuals, long households, long memberships,
        long visits, long formResponses, long answers,
        long skippedVisits, long skippedFormResponses, long skippedAnswers,
        long publishedForms, long questionsInPublishedForms
) {
    public long movable() {
        return individuals + households + memberships + visits + formResponses + answers;
    }
}
