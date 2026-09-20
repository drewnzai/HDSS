package com.andrew.hdss.dtos;

import com.andrew.hdss.models.enums.QuestionType;

public record CreateQuestionRequest(
        String name,
        String label,
        String hint,
        QuestionType type,
        boolean required,
        String relevant,
        String constraint,
        String constraintMessage,
        String calculation,
        String choiceListName
) {}