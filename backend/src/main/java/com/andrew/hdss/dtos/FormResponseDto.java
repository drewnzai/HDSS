package com.andrew.hdss.dtos;

import com.andrew.hdss.models.FormResponse;
import com.andrew.hdss.models.enums.FormResponseStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record FormResponseDto(
        Long id,
        String clientId,
        Long visitId,
        Long formId,
        String formName,
        String formTitle,
        Integer formVersion,
        FormResponseStatus status,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
    public static FormResponseDto from(FormResponse formResponse) {
        return new FormResponseDto(
                formResponse.getId(),
                formResponse.getClientId(),
                formResponse.getVisit().getId(),
                formResponse.getForm().getId(),
                formResponse.getForm().getName(),
                formResponse.getForm().getTitle(),
                formResponse.getFormVersion(),
                formResponse.getStatus(),
                formResponse.getStartedAt(),
                formResponse.getCompletedAt()
        );
    }
}
