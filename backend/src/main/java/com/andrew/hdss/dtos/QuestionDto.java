package com.andrew.hdss.dtos;// package: match your existing dto package

import com.andrew.hdss.models.Question;
import com.andrew.hdss.models.enums.QuestionType;

public record QuestionDto(
        Long id,
        Long formId,
        String name,
        String label,
        String hint,
        QuestionType type,
        boolean required,
        String relevant,
        String constraint,
        String constraintMessage,
        String calculation,
        String choiceListName,
        Integer orderIndex,
        MappedEntity mappedEntity,
        String mappedField
) {
    public static QuestionDto from(Question question) {
        return new QuestionDto(
                question.getId(),
                question.getForm().getId(),
                question.getName(),
                question.getLabel(),
                question.getHint(),
                question.getType(),
                question.isRequired(),
                question.getRelevant(),
                question.getConstraint(),
                question.getConstraintMessage(),
                question.getCalculation(),
                question.getChoiceListName(),
                question.getOrderIndex(),
                question.getMappedEntity(),
                question.getMappedField()
        );
    }
}
