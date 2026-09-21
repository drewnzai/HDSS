package com.andrew.hdss.services;

import com.andrew.hdss.dtos.CreateFormResponseRequest;
import com.andrew.hdss.dtos.FormResponseDto;
import com.andrew.hdss.models.*;
import com.andrew.hdss.models.enums.FormResponseStatus;
import com.andrew.hdss.models.enums.FormTarget;
import com.andrew.hdss.repositories.AnswerRepository;
import com.andrew.hdss.repositories.FormRepository;
import com.andrew.hdss.repositories.FormResponseRepository;
import com.andrew.hdss.repositories.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FormResponseService {

    private final FormResponseRepository formResponseRepository;
    private final FormRepository formRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final VisitService visitService;

    @Transactional
    public FormResponseDto startFormResponse(Long visitId, CreateFormResponseRequest request) {
        Visit visit = visitService.findVisitOrThrow(visitId);

        Form form = formRepository.findById(request.formId())
                .orElseThrow(() -> new EntityNotFoundException("Form not found: " + request.formId()));

        if (!form.isActive()) {
            throw new IllegalStateException("Form '" + form.getName() + "' is not active");
        }

        if (form.getTarget() == FormTarget.HOUSEHOLD && visit.getHousehold() == null) {
            throw new IllegalArgumentException(
                    "Form '" + form.getName() + "' requires a household, but this visit has none"
            );
        }
        if (form.getTarget() == FormTarget.INDIVIDUAL && visit.getIndividual() == null) {
            throw new IllegalArgumentException(
                    "Form '" + form.getName() + "' requires an individual, but this visit has none"
            );
        }

        if (formResponseRepository.findByClientId(request.clientId()).isPresent()) {
            throw new IllegalArgumentException(
                    "A form response with clientId '" + request.clientId() + "' already exists"
            );
        }

        FormResponse formResponse = new FormResponse();
        formResponse.setClientId(request.clientId());
        formResponse.setVisit(visit);
        formResponse.setForm(form);
        formResponse.setFormVersion(form.getVersion());
        formResponse.setStatus(FormResponseStatus.IN_PROGRESS);
        formResponse.setStartedAt(LocalDateTime.now());

        return FormResponseDto.from(formResponseRepository.save(formResponse));
    }

    @Transactional(readOnly = true)
    public List<FormResponseDto> getResponsesForVisit(Long visitId) {
        return formResponseRepository.findByVisitId(visitId).stream()
                .map(FormResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FormResponseDto getFormResponse(Long id) {
        return FormResponseDto.from(findFormResponseOrThrow(id));
    }

    @Transactional
    public FormResponseDto completeFormResponse(Long id) {
        FormResponse formResponse = findFormResponseOrThrow(id);

        if (formResponse.getStatus() == FormResponseStatus.COMPLETED) {
            throw new IllegalStateException("Form response " + id + " is already completed");
        }

        List<Question> requiredQuestions = questionRepository
                .findByFormIdOrderByOrderIndexAsc(formResponse.getForm().getId())
                .stream()
                .filter(Question::isRequired)
                .toList();

        Map<Long, Answer> answersByQuestionId = answerRepository.findByFormResponseId(id).stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a));

        List<String> missingLabels = requiredQuestions.stream()
                .filter(q -> {
                    Answer a = answersByQuestionId.get(q.getId());
                    return a == null || a.getValue() == null || a.getValue().isBlank();
                })
                .map(Question::getLabel)
                .toList();

        if (!missingLabels.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot complete: missing required answers for: " + String.join(", ", missingLabels)
            );
        }

        formResponse.setStatus(FormResponseStatus.COMPLETED);
        formResponse.setCompletedAt(LocalDateTime.now());

        return FormResponseDto.from(formResponseRepository.save(formResponse));
    }

    protected FormResponse findFormResponseOrThrow(Long id) {
        return formResponseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form response not found: " + id));
    }
}
