package com.andrew.hdss.dtos;

import com.andrew.hdss.models.Round;
import com.andrew.hdss.models.User;
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
        LocalDateTime createdAt,
        String createdBy
) {

    public static RoundDto from(Round round) {
        User user = round.getCreatedBy();
        String username = user.getFirstName() + " " + user.getLastName();

        return new RoundDto(
                round.getId(),
                round.getRoundName(),
                round.getDatabaseName(),
                round.getStatus(),
                round.isValidationPassed(),
                round.getStartedAt(),
                round.getCompletedAt(),
                round.getErrorMessage(),
                round.getCreatedAt(),
                username
        );
    }
}