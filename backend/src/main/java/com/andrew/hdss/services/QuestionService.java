package com.andrew.hdss.services;

import com.andrew.hdss.dtos.CreateQuestionRequest;
import com.andrew.hdss.dtos.QuestionDto;
import com.andrew.hdss.dtos.ReorderQuestionsRequest;
import com.andrew.hdss.dtos.UpdateQuestionRequest;
import com.andrew.hdss.models.Form;
import com.andrew.hdss.models.Question;
import com.andrew.hdss.models.enums.MappedEntity;
import com.andrew.hdss.repositories.FormRepository;
import com.andrew.hdss.repositories.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final FormRepository formRepository;
    private final FormService formService;

    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestions(Long formId) {
        return questionRepository.findByFormIdOrderByOrderIndexAsc(formId).stream()
                .map(QuestionDto::from)
                .toList();
    }

    @Transactional
    public QuestionDto addQuestion(Long formId, CreateQuestionRequest request) {
        Form form = findFormOrThrow(formId);
        formService.assertEditable(form);

        int nextOrderIndex = (int) questionRepository.countByFormId(formId);

        Question question = new Question();
        question.setForm(form);
        question.setName(request.name());
        question.setLabel(request.label());
        question.setHint(request.hint());
        question.setType(request.type());
        question.setRequired(request.required());
        question.setRelevant(request.relevant());
        question.setConstraint(request.constraint());
        question.setConstraintMessage(request.constraintMessage());
        question.setCalculation(request.calculation());
        question.setChoiceListName(request.choiceListName());
        question.setOrderIndex(nextOrderIndex);
        question.setMappedEntity(request.mappedEntity() != null ? request.mappedEntity() : MappedEntity.NONE);
        question.setMappedField(request.mappedField());

        return QuestionDto.from(questionRepository.save(question));
    }

    @Transactional
    public QuestionDto updateQuestion(Long formId, Long questionId, UpdateQuestionRequest request) {
        Question question = findQuestionInForm(formId, questionId);
        formService.assertEditable(question.getForm());

        question.setLabel(request.label());
        question.setHint(request.hint());
        question.setType(request.type());
        question.setRequired(request.required());
        question.setRelevant(request.relevant());
        question.setConstraint(request.constraint());
        question.setConstraintMessage(request.constraintMessage());
        question.setCalculation(request.calculation());
        question.setChoiceListName(request.choiceListName());
        question.setMappedEntity(request.mappedEntity() != null ? request.mappedEntity() : MappedEntity.NONE);
        question.setMappedField(request.mappedField());

        return QuestionDto.from(questionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long formId, Long questionId) {
        Question question = findQuestionInForm(formId, questionId);
        formService.assertEditable(question.getForm());
        questionRepository.delete(question);
    }

    @Transactional
    public List<QuestionDto> reorderQuestions(Long formId, ReorderQuestionsRequest request) {
        Form form = findFormOrThrow(formId);
        formService.assertEditable(form);

        List<Question> questions = questionRepository.findByFormIdOrderByOrderIndexAsc(formId);
        Map<Long, Question> byId = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        if (!byId.keySet().equals(new HashSet<>(request.questionIdsInOrder()))) {
            throw new IllegalArgumentException(
                    "Reorder list must contain exactly this form's existing question ids, no more, no fewer"
            );
        }

        List<Question> reordered = new ArrayList<>();
        for (int i = 0; i < request.questionIdsInOrder().size(); i++) {
            Question q = byId.get(request.questionIdsInOrder().get(i));
            q.setOrderIndex(i);
            reordered.add(q);
        }

        return questionRepository.saveAll(reordered).stream()
                .map(QuestionDto::from)
                .toList();
    }

    private Question findQuestionInForm(Long formId, Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + questionId));
        if (!question.getForm().getId().equals(formId)) {
            throw new IllegalArgumentException(
                    "Question " + questionId + " does not belong to form " + formId
            );
        }
        return question;
    }

    private Form findFormOrThrow(Long formId) {
        return formRepository.findById(formId)
                .orElseThrow(() -> new EntityNotFoundException("Form not found: " + formId));
    }
}
