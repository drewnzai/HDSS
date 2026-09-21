package com.andrew.hdss.dtos;

import com.andrew.hdss.models.Visit;
import com.andrew.hdss.models.enums.VisitStatus;

import java.time.LocalDateTime;

public record VisitDto(
        Long id,
        String clientId,
        Long householdId,
        Long individualId,
        LocalDateTime visitDate,
        Long conductedById,
        String conductedByName,
        VisitStatus status
) {
    public static VisitDto from(Visit visit) {
        return new VisitDto(
                visit.getId(),
                visit.getClientId(),
                visit.getHousehold() != null ? visit.getHousehold().getId() : null,
                visit.getIndividual() != null ? visit.getIndividual().getId() : null,
                visit.getVisitDate(),
                visit.getConductedBy().getId(),
                visit.getConductedBy().getFirstName() + " " + visit.getConductedBy().getLastName(),
                visit.getStatus()
        );
    }
}
