package com.andrew.hdss.services;

import com.andrew.hdss.dtos.AnswerDto;
import com.andrew.hdss.dtos.SubmitAnswerRequest;
import com.andrew.hdss.models.Answer;
import com.andrew.hdss.models.Choice;
import com.andrew.hdss.models.FormResponse;
import com.andrew.hdss.models.Question;
import com.andrew.hdss.models.enums.FormResponseStatus;
import com.andrew.hdss.repositories.AnswerRepository;
import com.andrew.hdss.repositories.ChoiceRepository;
import com.andrew.hdss.repositories.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final FormResponseService formResponseService;

    @Transactional
    public AnswerDto submitAnswer(Long formResponseId, SubmitAnswerRequest request) {
        FormResponse formResponse = formResponseService.findFormResponseOrThrow(formResponseId);

        if (formResponse.getStatus() == FormResponseStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Form response " + formResponseId + " is already completed and can no longer be edited"
            );
        }

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + request.questionId()));

        if (!question.getForm().getId().equals(formResponse.getForm().getId())) {
            throw new IllegalArgumentException(
                    "Question " + request.questionId() + " does not belong to this form response's form"
            );
        }

        validateValue(question, request.value());

        // Upsert per (formResponse, question) — resubmitting an answer for
        // the same question updates it rather than creating a duplicate row,
        // since a user changing their mind mid-form is the normal case.
        Answer answer = answerRepository
                .findByFormResponseIdAndQuestionId(formResponseId, request.questionId())
                .orElseGet(() -> {
                    Answer a = new Answer();
                    a.setClientId(request.clientId());
                    a.setFormResponse(formResponse);
                    a.setQuestion(question);
                    return a;
                });

        answer.setValue(request.value());

        return AnswerDto.from(answerRepository.save(answer));
    }

    @Transactional(readOnly = true)
    public List<AnswerDto> getAnswers(Long formResponseId) {
        return answerRepository.findByFormResponseId(formResponseId).stream()
                .map(AnswerDto::from)
                .toList();
    }

    /**
     * Format-level validation only (does this look like a valid integer /
     * date / choice value) — NOT the XPath-style relevant/constraint
     * expressions from Question, which are stored as-is and not evaluated
     * yet (see decisions-and-patterns: that's a separate future phase).
     */
    private void validateValue(Question question, String value) {
        if (value == null || value.isBlank()) {
            // Absence is handled separately by
            // FormResponseService.completeFormResponse()'s required check.
            return;
        }

        switch (question.getType()) {
            case INTEGER -> {
                try {
                    Long.parseLong(value.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "'" + value + "' is not a valid integer for question '" + question.getName() + "'"
                    );
                }
            }
            case DECIMAL -> {
                try {
                    Double.parseDouble(value.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "'" + value + "' is not a valid decimal for question '" + question.getName() + "'"
                    );
                }
            }
            case DATE -> {
                try {
                    LocalDate.parse(value.trim());
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException(
                            "'" + value + "' is not a valid date for question '" + question.getName() + "'"
                    );
                }
            }
            case TIME -> {
                try {
                    LocalTime.parse(value.trim());
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException(
                            "'" + value + "' is not a valid time for question '" + question.getName() + "'"
                    );
                }
            }
            case DATETIME -> {
                try {
                    LocalDateTime.parse(value.trim());
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException(
                            "'" + value + "' is not a valid datetime for question '" + question.getName() + "'"
                    );
                }
            }
            case SELECT_ONE -> {
                Set<String> validNames = choiceNames(question.getChoiceListName());
                if (!validNames.contains(value.trim())) {
                    throw new IllegalArgumentException(
                            "'" + value + "' is not a valid choice for question '" + question.getName() + "'"
                    );
                }
            }
            case SELECT_MULTIPLE -> {
                Set<String> validNames = choiceNames(question.getChoiceListName());
                for (String token : value.trim().split("\\s+")) {
                    if (!validNames.contains(token)) {
                        throw new IllegalArgumentException(
                                "'" + token + "' is not a valid choice for question '" + question.getName() + "'"
                        );
                    }
                }
            }
            case GEOPOINT -> {
                // ODK geopoint format: "lat lon [altitude] [accuracy]" —
                // only lat/lon are actually checked here.
                String[] parts = value.trim().split("\\s+");
                if (parts.length < 2) {
                    throw new IllegalArgumentException(
                            "Geopoint for question '" + question.getName() + "' must include latitude and longitude"
                    );
                }
                try {
                    Double.parseDouble(parts[0]);
                    Double.parseDouble(parts[1]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Geopoint for question '" + question.getName() + "' has an invalid latitude/longitude"
                    );
                }
            }
            // TEXT, NOTE, ACKNOWLEDGE, CALCULATE, BARCODE, IMAGE, AUDIO,
            // VIDEO: no format constraint beyond being a string. IMAGE/
            // AUDIO/VIDEO are expected to hold a file reference — actual
            // media upload/storage isn't part of this model yet.
            default -> {}
        }
    }

    private Set<String> choiceNames(String listName) {
        if (listName == null) {
            return Set.of();
        }
        return choiceRepository.findByListName(listName).stream()
                .map(Choice::getName)
                .collect(Collectors.toSet());
    }
}
