package com.andrew.hdss.dtos;// package: match your existing dto package

import com.andrew.hdss.models.Visit;
import com.andrew.hdss.models.enums.VisitStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record VisitDto(
        Long id,
        String clientId,
        Long householdId,
        LocalDateTime visitDate,
        VisitStatus status
) {
    public static VisitDto from(Visit visit) {
        return new VisitDto(
                visit.getId(),
                visit.getClientId(),
                visit.getHousehold() != null ? visit.getHousehold().getId() : null,
                visit.getVisitDate(),
                visit.getStatus()
        );
    }
}
