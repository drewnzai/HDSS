package com.andrew.hdss.dtos;// package: match your existing dto package

import java.time.Instant;
import java.time.LocalDateTime;

public record CreateVisitRequest(
        String clientId,
        Long householdId,
        Long individualId,
        LocalDateTime visitDate
) {}
