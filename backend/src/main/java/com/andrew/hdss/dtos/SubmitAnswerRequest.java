package com.andrew.hdss.dtos;

public record SubmitAnswerRequest(
        String clientId,
        Long questionId,
        String value
) {}
