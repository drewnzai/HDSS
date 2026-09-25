package com.andrew.hdss.dtos;// package: match your existing dto package

import com.andrew.hdss.models.enums.VisitStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record VisitPushDto(
        String clientId,
        String householdClientId,
        String individualClientId,
        LocalDateTime visitDate,
        VisitStatus status
) {}
