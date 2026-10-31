package com.andrew.hdss.dtos;

import com.andrew.hdss.models.enums.RoundStatus;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record RoundDetailsDto(
        Long id,
        String roundName,
        String databaseName,
        RoundStatus status,
        boolean validationPassed,
        JsonNode validationReport,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String errorMessage,
        LocalDateTime createdAt
) {
}