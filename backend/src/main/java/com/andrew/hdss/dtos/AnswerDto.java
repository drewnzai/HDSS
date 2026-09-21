package com.andrew.hdss.dtos;

import com.andrew.hdss.models.Answer;

public record AnswerDto(
        Long id,
        String clientId,
        Long formResponseId,
        Long questionId,
        String questionName,
        String value
) {
    public static AnswerDto from(Answer answer) {
        return new AnswerDto(
                answer.getId(),
                answer.getClientId(),
                answer.getFormResponse().getId(),
                answer.getQuestion().getId(),
                answer.getQuestion().getName(),
                answer.getValue()
        );
    }
}