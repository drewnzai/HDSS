package com.andrew.hdss.dtos;

public record AnswerPushDto(
        String clientId,
        String formResponseClientId,
        Long questionId,
        String value
) {}
