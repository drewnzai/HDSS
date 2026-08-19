package com.andrew.hdss.dtos.sync;

import java.util.List;

public record SyncPushRequest(
        List<HouseholdPushDto> households,
        List<IndividualPushDto> individuals,
        List<MembershipPushDto> memberships
) {
    public SyncPushRequest {
        households = households == null ? List.of() : households;
        individuals = individuals == null ? List.of() : individuals;
        memberships = memberships == null ? List.of() : memberships;
    }
}
