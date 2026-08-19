package com.andrew.hdss.dtos;

import com.andrew.hdss.models.Membership;
import com.andrew.hdss.models.enums.MembershipEndType;
import com.andrew.hdss.models.enums.MembershipStartType;
import com.andrew.hdss.models.enums.RelationshipToHead;

import java.time.LocalDate;

public record MembershipDto(
        Long id,
        String clientId,
        Long individualId,
        Long householdId,
        RelationshipToHead relationshipToHead,
        LocalDate startDate,
        MembershipStartType startType,
        LocalDate endDate,
        MembershipEndType endType
) {
    public static MembershipDto from(Membership m) {
        return new MembershipDto(
                m.getId(),
                m.getClientId(),
                m.getIndividual().getId(),
                m.getHousehold().getId(),
                m.getRelationshipToHead(),
                m.getStartDate(),
                m.getStartType(),
                m.getEndDate(),
                m.getEndType()
        );
    }
}