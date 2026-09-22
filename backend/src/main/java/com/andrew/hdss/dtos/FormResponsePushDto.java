package com.andrew.hdss.dtos;

import com.andrew.hdss.models.enums.FormResponseStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record FormResponsePushDto(
        String clientId,
        String visitClientId,
        Long formId,
        Integer formVersion,
        FormResponseStatus status,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {}
