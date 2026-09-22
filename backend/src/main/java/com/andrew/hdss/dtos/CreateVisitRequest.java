package com.andrew.hdss.dtos;// package: match your existing dto package

import java.time.Instant;

public record CreateVisitRequest(
        String clientId,
        Long householdId,
        Long individualId,
        Instant visitDate
) {}
