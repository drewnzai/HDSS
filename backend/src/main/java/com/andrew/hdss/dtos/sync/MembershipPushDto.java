package com.andrew.hdss.dtos.sync;

import com.andrew.hdss.models.enums.MembershipEndType;
import com.andrew.hdss.models.enums.MembershipStartType;
import com.andrew.hdss.models.enums.RelationshipToHead;

import java.time.LocalDate;

public record MembershipPushDto(
        String clientId,
        String individualClientId,
        String householdClientId,
        RelationshipToHead relationshipToHead,
        LocalDate startDate,
        MembershipStartType startType,
        LocalDate endDate,
        MembershipEndType endType
) {}
