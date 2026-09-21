package com.andrew.hdss.dtos;

import java.time.LocalDateTime;

public record CreateVisitRequest(
        String clientId,
        Long householdId,
        Long individualId,
        LocalDateTime visitDate
) {}