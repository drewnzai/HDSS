package com.andrew.hdss.dtos;// package: match your existing dto package
// formId/questionId reference synced-down reference data by their real
// server ids (same as Household.locationId) — no clientId needed for those,
// unlike visitClientId/formResponseClientId below which point at
// offline-created records.

import com.andrew.hdss.models.enums.FormResponseStatus;

import java.time.Instant;

public record FormResponsePushDto(
        String clientId,
        String visitClientId,
        Long formId,
        Integer formVersion,
        FormResponseStatus status,
        Instant startedAt,
        Instant completedAt
) {}
