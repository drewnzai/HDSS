package com.andrew.hdss.dtos;

import com.andrew.hdss.models.Round;
import com.andrew.hdss.models.enums.RoundStatus;

import java.time.LocalDateTime;

public record RoundDto(
        Long id,
        String roundName,
        String databaseName,
        RoundStatus status,
        boolean validationPassed,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String errorMessage,
        LocalDateTime createdAt
) {

    public static RoundDto from(Round round) {
        return new RoundDto(
                round.getId(),
                round.getRoundName(),
                round.getDatabaseName(),
                round.getStatus(),
                round.isValidationPassed(),
                round.getStartedAt(),
                round.getCompletedAt(),
                round.getErrorMessage(),
                round.getCreatedAt()
        );
    }
}