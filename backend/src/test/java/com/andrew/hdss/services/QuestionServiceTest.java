package com.andrew.hdss.services;

import com.andrew.hdss.dtos.CreateQuestionRequest;
import com.andrew.hdss.dtos.QuestionDto;
import com.andrew.hdss.dtos.ReorderQuestionsRequest;
import com.andrew.hdss.dtos.UpdateQuestionRequest;
import com.andrew.hdss.exceptions.EntityNotFoundException;
import com.andrew.hdss.models.Form;
import com.andrew.hdss.models.Question;
import com.andrew.hdss.models.enums.FormCategory;
import com.andrew.hdss.models.enums.FormStatus;
import com.andrew.hdss.models.enums.FormTarget;
import com.andrew.hdss.models.enums.MappedEntity;
import com.andrew.hdss.repositories.FormRepository;
import com.andrew.hdss.repositories.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private FormRepository formRepository;

    @Mock
    private FormService formService;

    @InjectMocks
    private QuestionService questionService;

    private Form form;
    private Form otherForm;

    private Question question1;
    private Question question2;

    @BeforeEach
    void setUp() {
        form = new Form();
        form.setId(1L);
        form.setName("household");
        form.setTitle("Household Form");
        form.setCategory(FormCategory.CORE);
        form.setTarget(FormTarget.HOUSEHOLD);
        form.setStatus(FormStatus.DRAFT);

        otherForm = new Form();
        otherForm.setId(2L);
        otherForm.setName("individual");
        otherForm.setTitle("Individual Form");
        otherForm.setCategory(FormCategory.CORE);
        otherForm.setTarget(FormTarget.INDIVIDUAL);
        otherForm.setStatus(FormStatus.DRAFT);

        question1 = new Question();
        question1.setId(10L);
        question1.setForm(form);
        question1.setName("first_name");
        question1.setLabel("First Name");
        question1.setHint("Enter first name");
        question1.setOrderIndex(0);
        question1.setRequired(true);
        question1.setMappedEntity(MappedEntity.NONE);

        question2 = new Question();
        question2.setId(20L);
        question2.setForm(form);
        question2.setName("last_name");
        question2.setLabel("Last Name");
        question2.setHint("Enter last name");
        question2.setOrderIndex(1);
        question2.setRequired(true);
        question2.setMappedEntity(MappedEntity.NONE);
    }

    // =========================================================================
    // getQuestions()
    // =========================================================================

    @Test
    void shouldGetQuestionsForForm() {
        when(questionRepository.findByFormIdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of(question1, question2));

        List<QuestionDto> result =
                questionService.getQuestions(1L);

        assertThat(result)
                .hasSize(2);

        assertThat(result.get(0))
                .isNotNull();

        assertThat(result.get(1))
                .isNotNull();

        verify(questionRepository)
                .findByFormIdOrderByOrderIndexAsc(1L);
    }

    @Test
    void shouldReturnEmptyListWhenFormHasNoQuestions() {
        when(questionRepository.findByFormIdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of());

        List<QuestionDto> result =
                questionService.getQuestions(1L);

        assertThat(result)
                .isEmpty();

        verify(questionRepository)
                .findByFormIdOrderByOrderIndexAsc(1L);
    }

    // =========================================================================
    // addQuestion()
    // =========================================================================

    @Test
    void shouldAddQuestionWithNextOrderIndex() {
        CreateQuestionRequest request = new CreateQuestionRequest(
                "age",
                "Age",
                "Enter age",
                null,
                false,
                "",
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(formRepository.findById(1L))
                .thenReturn(Optional.of(form));

        doNothing()
                .when(formService)
                .assertEditable(form);

        when(questionRepository.countByFormId(1L))
                .thenReturn(2L);

        when(questionRepository.save(any(Question.class)))
                .thenReturn(question1);

        QuestionDto result =
                questionService.addQuestion(1L, request);

        assertThat(result)
                .isNotNull();

        verify(formRepository)
                .findById(1L);

        verify(formService)
                .assertEditable(form);

        verify(questionRepository)
                .countByFormId(1L);

        ArgumentCaptor<Question> captor =
                ArgumentCaptor.forClass(Question.class);

        verify(questionRepository)
                .save(captor.capture());

        Question saved = captor.getValue();

        assertThat(saved.getForm())
                .isSameAs(form);

        assertThat(saved.getName())
                .isEqualTo("age");

        assertThat(saved.getLabel())
                .isEqualTo("Age");

        assertThat(saved.getHint())
                .isEqualTo("Enter age");

        assertThat(saved.getOrderIndex())
                .isEqualTo(2);

        assertThat(saved.getMappedEntity())
                .isEqualTo(MappedEntity.NONE);
    }

    @Test
    void shouldUseRequestedMappedEntityWhenProvided() {
        CreateQuestionRequest request = new CreateQuestionRequest(
                "household_code",
                "Household Code",
                null,
                null,
                true,
                "",
                null,
                null,
                null,
                null,
                MappedEntity.HOUSEHOLD,
                "householdCode"
        );

        when(formRepository.findById(1L))
                .thenReturn(Optional.of(form));

        when(questionRepository.countByFormId(1L))
                .thenReturn(0L);

        when(questionRepository.save(any(Question.class)))
                .thenReturn(question1);

        questionService.addQuestion(1L, request);

        ArgumentCaptor<Question> captor =
                ArgumentCaptor.forClass(Question.class);

        verify(questionRepository)
                .save(captor.capture());

        Question saved = captor.getValue();

        assertThat(saved.getMappedEntity())
                .isEqualTo(MappedEntity.HOUSEHOLD);

        assertThat(saved.getMappedField())
                .isEqualTo("householdCode");
    }

    @Test
    void shouldRejectAddingQuestionToNonexistentForm() {
        CreateQuestionRequest request = mock(CreateQuestionRequest.class);

        when(formRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                questionService.addQuestion(99L, request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Form not found: 99");

        verify(formRepository)
                .findById(99L);

        verify(formService, never())
                .assertEditable(any(Form.class));

        verify(questionRepository, never())
                .save(any(Question.class));
    }

    // =========================================================================
    // updateQuestion()
    // =========================================================================

    @Test
    void shouldUpdateQuestion() {
        UpdateQuestionRequest request = new UpdateQuestionRequest(
                "Updated Label",
                "Updated hint",
                null,
                false,
                "",
                "age >= 18",
                "Age must be 18 or older",
                null,
                null,
                MappedEntity.NONE,
                null
        );

        when(questionRepository.findById(10L))
                .thenReturn(Optional.of(question1));

        when(questionRepository.save(any(Question.class)))
                .thenReturn(question1);

        QuestionDto result =
                questionService.updateQuestion(1L, 10L, request);

        assertThat(result)
                .isNotNull();

        verify(questionRepository)
                .findById(10L);

        verify(formService)
                .assertEditable(form);

        ArgumentCaptor<Question> captor =
                ArgumentCaptor.forClass(Question.class);

        verify(questionRepository)
                .save(captor.capture());

        Question updated = captor.getValue();

        assertThat(updated.getLabel())
                .isEqualTo("Updated Label");

        assertThat(updated.getHint())
                .isEqualTo("Updated hint");

        assertThat(updated.isRequired())
                .isFalse();

        assertThat(updated.getRelevant())
                .isEqualTo("");

        assertThat(updated.getConstraint())
                .isEqualTo("age >= 18");

        assertThat(updated.getConstraintMessage())
                .isEqualTo("Age must be 18 or older");

        assertThat(updated.getMappedEntity())
                .isEqualTo(MappedEntity.NONE);
    }

    @Test
    void shouldUseNoneWhenUpdateMappedEntityIsNull() {
        UpdateQuestionRequest request = new UpdateQuestionRequest(
                "Updated Label",
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        question1.setMappedEntity(MappedEntity.HOUSEHOLD);

        when(questionRepository.findById(10L))
                .thenReturn(Optional.of(question1));

        when(questionRepository.save(any(Question.class)))
                .thenReturn(question1);

        questionService.updateQuestion(1L, 10L, request);

        assertThat(question1.getMappedEntity())
                .isEqualTo(MappedEntity.NONE);

        verify(questionRepository)
                .save(question1);
    }

    @Test
    void shouldThrowWhenUpdatingNonexistentQuestion() {
        UpdateQuestionRequest request =
                mock(UpdateQuestionRequest.class);

        when(questionRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                questionService.updateQuestion(1L, 99L, request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Question not found: 99");

        verify(questionRepository)
                .findById(99L);

        verify(formService, never())
                .assertEditable(any(Form.class));

        verify(questionRepository, never())
                .save(any(Question.class));
    }

    @Test
    void shouldRejectQuestionThatDoesNotBelongToForm() {
        question1.setForm(otherForm);

        UpdateQuestionRequest request =
                mock(UpdateQuestionRequest.class);

        when(questionRepository.findById(10L))
                .thenReturn(Optional.of(question1));

        assertThatThrownBy(() ->
                questionService.updateQuestion(1L, 10L, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Question 10 does not belong to form 1"
                );

        verify(questionRepository)
                .findById(10L);

        verify(formService, never())
                .assertEditable(any(Form.class));

        verify(questionRepository, never())
                .save(any(Question.class));
    }

    // =========================================================================
    // deleteQuestion()
    // =========================================================================

    @Test
    void shouldDeleteQuestion() {
        when(questionRepository.findById(10L))
                .thenReturn(Optional.of(question1));

        questionService.deleteQuestion(1L, 10L);

        verify(questionRepository)
                .findById(10L);

        verify(formService)
                .assertEditable(form);

        verify(questionRepository)
                .delete(question1);
    }

    @Test
    void shouldNotDeleteQuestionThatDoesNotBelongToForm() {
        question1.setForm(otherForm);

        when(questionRepository.findById(10L))
                .thenReturn(Optional.of(question1));

        assertThatThrownBy(() ->
                questionService.deleteQuestion(1L, 10L)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Question 10 does not belong to form 1"
                );

        verify(formService, never())
                .assertEditable(any(Form.class));

        verify(questionRepository, never())
                .delete(any(Question.class));
    }

    @Test
    void shouldNotDeleteQuestionWhenQuestionDoesNotExist() {
        when(questionRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                questionService.deleteQuestion(1L, 99L)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Question not found: 99");

        verify(formService, never())
                .assertEditable(any(Form.class));

        verify(questionRepository, never())
                .delete(any(Question.class));
    }

    // =========================================================================
    // reorderQuestions()
    // =========================================================================

    @Test
    void shouldReorderQuestions() {
        ReorderQuestionsRequest request =
                new ReorderQuestionsRequest(
                        List.of(20L, 10L)
                );

        when(formRepository.findById(1L))
                .thenReturn(Optional.of(form));

        when(questionRepository
                .findByFormIdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of(question1, question2));

        when(questionRepository.saveAll(anyList()))
                .thenReturn(List.of(question2, question1));

        List<QuestionDto> result =
                questionService.reorderQuestions(1L, request);

        assertThat(result)
                .hasSize(2);

        verify(formRepository)
                .findById(1L);

        verify(formService)
                .assertEditable(form);

        verify(questionRepository)
                .findByFormIdOrderByOrderIndexAsc(1L);

        ArgumentCaptor<List<Question>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(questionRepository)
                .saveAll(captor.capture());

        List<Question> reordered =
                captor.getValue();

        assertThat(reordered)
                .containsExactly(question2, question1);

        assertThat(question2.getOrderIndex())
                .isZero();

        assertThat(question1.getOrderIndex())
                .isEqualTo(1);
    }

    @Test
    void shouldRejectReorderWithMissingQuestionId() {
        ReorderQuestionsRequest request =
                new ReorderQuestionsRequest(
                        List.of(10L)
                );

        when(formRepository.findById(1L))
                .thenReturn(Optional.of(form));

        when(questionRepository
                .findByFormIdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of(question1, question2));

        assertThatThrownBy(() ->
                questionService.reorderQuestions(1L, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Reorder list must contain exactly this form's existing " +
                                "question ids, no more, no fewer"
                );

        verify(questionRepository, never())
                .saveAll(anyList());
    }

    @Test
    void shouldRejectReorderWithUnknownQuestionId() {
        ReorderQuestionsRequest request =
                new ReorderQuestionsRequest(
                        List.of(10L, 20L, 99L)
                );

        when(formRepository.findById(1L))
                .thenReturn(Optional.of(form));

        when(questionRepository
                .findByFormIdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of(question1, question2));

        assertThatThrownBy(() ->
                questionService.reorderQuestions(1L, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Reorder list must contain exactly this form's existing " +
                                "question ids, no more, no fewer"
                );

        verify(questionRepository, never())
                .saveAll(anyList());
    }

    @Test
    void shouldRejectReorderWithDuplicateQuestionId() {
        ReorderQuestionsRequest request =
                new ReorderQuestionsRequest(
                        List.of(10L, 10L)
                );

        when(formRepository.findById(1L))
                .thenReturn(Optional.of(form));

        when(questionRepository
                .findByFormIdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of(question1, question2));

        assertThatThrownBy(() ->
                questionService.reorderQuestions(1L, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Reorder list must contain exactly this form's existing " +
                                "question ids, no more, no fewer"
                );

        verify(questionRepository, never())
                .saveAll(anyList());
    }

    @Test
    void shouldRejectReorderForNonexistentForm() {
        ReorderQuestionsRequest request =
                new ReorderQuestionsRequest(
                        List.of(10L, 20L)
                );

        when(formRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                questionService.reorderQuestions(99L, request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Form not found: 99");

        verify(questionRepository, never())
                .findByFormIdOrderByOrderIndexAsc(anyLong());

        verify(questionRepository, never())
                .saveAll(anyList());
    }
}
