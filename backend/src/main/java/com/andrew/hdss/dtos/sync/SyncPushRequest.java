package com.andrew.hdss.dtos.sync;

import com.andrew.hdss.dtos.*;

import java.util.List;

public record SyncPushRequest(
        List<HouseholdPushDto> households,
        List<IndividualPushDto> individuals,
        List<MembershipPushDto> memberships,
        List<AnswerPushDto> answers,
        List<VisitPushDto> visits,
        List<FormResponsePushDto> formResponses
) {
    public SyncPushRequest {
        households = households == null ? List.of() : households;
        individuals = individuals == null ? List.of() : individuals;
        memberships = memberships == null ? List.of() : memberships;
        answers = answers == null ? List.of(): answers;
        visits = visits == null ? List.of(): visits;
        formResponses = formResponses == null ? List.of(): formResponses;
    }
}
