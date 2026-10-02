package com.andrew.hdss.services;

import com.andrew.hdss.dtos.*;
import com.andrew.hdss.exceptions.EntityNotFoundException;
import com.andrew.hdss.exceptions.FormLockedException;
import com.andrew.hdss.models.Form;
import com.andrew.hdss.models.enums.FormCategory;
import com.andrew.hdss.models.enums.FormStatus;
import com.andrew.hdss.models.enums.FormTarget;
import com.andrew.hdss.repositories.FormRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormServiceTest {

    @Mock
    private FormRepository formRepository;

    @InjectMocks
    private FormService formService;

    private Form draftForm;
    private Form publishedForm;

    @BeforeEach
    void setUp() {
        draftForm = new Form();
        draftForm.setId(1L);
        draftForm.setName("household");
        draftForm.setTitle("Household Form");
        draftForm.setCategory(FormCategory.CORE);
        draftForm.setTarget(FormTarget.HOUSEHOLD);
        draftForm.setDescription("Household registration form");
        draftForm.setVersion(1);
        draftForm.setActive(false);
        draftForm.setStatus(FormStatus.DRAFT);

        publishedForm = new Form();
        publishedForm.setId(2L);
        publishedForm.setName("individual");
        publishedForm.setTitle("Individual Form");
        publishedForm.setCategory(FormCategory.CORE);
        publishedForm.setTarget(FormTarget.INDIVIDUAL);
        publishedForm.setDescription("Individual registration form");
        publishedForm.setVersion(1);
        publishedForm.setActive(true);
        publishedForm.setStatus(FormStatus.PUBLISHED);
    }

    // =========================================================================
    // getAllForms()
    // =========================================================================

    @Test
    void shouldGetAllForms() {
        ResourceRequest request = new ResourceRequest();
        request.setPage(0);
        request.setSize(10);

        Page<Form> page = new PageImpl<>(
                List.of(draftForm, publishedForm)
        );

        when(formRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        BatchResponse<FormDto> result =
                formService.getAllForms(request);

        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getData()).hasSize(2);

        verify(formRepository).findAll(any(Pageable.class));
    }

    @Test
    void shouldReturnEmptyPageWhenThereAreNoForms() {
        ResourceRequest request = new ResourceRequest();
        request.setPage(0);
        request.setSize(10);

        Page<Form> page = new PageImpl<>(List.of());

        when(formRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        BatchResponse<FormDto> result =
                formService.getAllForms(request);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(formRepository).findAll(any(Pageable.class));
    }

    // =========================================================================
    // getForm()
    // =========================================================================

    @Test
    void shouldGetForm() {
        when(formRepository.findById(1L))
                .thenReturn(Optional.of(draftForm));

        FormDto result = formService.getForm(1L);

        assertThat(result).isNotNull();

        verify(formRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenGettingNonexistentForm() {
        when(formRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                formService.getForm(99L)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Form not found: 99");

        verify(formRepository).findById(99L);
    }

    // =========================================================================
    // createForm()
    // =========================================================================

    @Test
    void shouldCreateForm() {
        CreateFormRequest request = new CreateFormRequest(
                "household",
                "Household Form",
                FormCategory.CORE,
                FormTarget.HOUSEHOLD,
                "Household registration form"
        );

        when(formRepository.findByName("household"))
                .thenReturn(Optional.empty());

        when(formRepository.save(any(Form.class)))
                .thenReturn(draftForm);

        FormDto result = formService.createForm(request);

        assertThat(result).isNotNull();

        verify(formRepository).findByName("household");
        verify(formRepository).save(any(Form.class));
    }

    @Test
    void shouldCreateFormWithCorrectInitialValues() {
        CreateFormRequest request = new CreateFormRequest(
                "household",
                "Household Form",
                FormCategory.CORE,
                FormTarget.HOUSEHOLD,
                "Household registration form"
        );

        when(formRepository.findByName("household"))
                .thenReturn(Optional.empty());

        when(formRepository.save(any(Form.class)))
                .thenReturn(draftForm);

        formService.createForm(request);

        ArgumentCaptor<Form> captor =
                ArgumentCaptor.forClass(Form.class);

        verify(formRepository).save(captor.capture());

        Form savedForm = captor.getValue();

        assertThat(savedForm.getName())
                .isEqualTo("household");

        assertThat(savedForm.getTitle())
                .isEqualTo("Household Form");

        assertThat(savedForm.getCategory())
                .isEqualTo(FormCategory.CORE);

        assertThat(savedForm.getTarget())
                .isEqualTo(FormTarget.HOUSEHOLD);

        assertThat(savedForm.getDescription())
                .isEqualTo("Household registration form");

        assertThat(savedForm.getVersion())
                .isEqualTo(1);

        assertThat(savedForm.isActive())
                .isFalse();

        assertThat(savedForm.getStatus())
                .isEqualTo(FormStatus.DRAFT);
    }

    @Test
    void shouldRejectDuplicateFormName() {
        CreateFormRequest request = new CreateFormRequest(
                "household",
                "Household Form",
                FormCategory.CORE,
                FormTarget.HOUSEHOLD,
                "Household registration form"
        );

        when(formRepository.findByName("household"))
                .thenReturn(Optional.of(draftForm));

        assertThatThrownBy(() ->
                formService.createForm(request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "A form named 'household' already exists"
                );

        verify(formRepository).findByName("household");

        verify(formRepository, never())
                .save(any(Form.class));
    }

    // =========================================================================
    // updateForm()
    // =========================================================================

    @Test
    void shouldUpdateDraftForm() {
        UpdateFormRequest request = new UpdateFormRequest(
                "Updated Household Form",
                FormCategory.EXTRA,
                FormTarget.HOUSEHOLD,
                "Updated description",
                true
        );

        when(formRepository.findById(1L))
                .thenReturn(Optional.of(draftForm));

        when(formRepository.save(any(Form.class)))
                .thenReturn(draftForm);

        FormDto result =
                formService.updateForm(1L, request);

        assertThat(result).isNotNull();

        ArgumentCaptor<Form> captor =
                ArgumentCaptor.forClass(Form.class);

        verify(formRepository).save(captor.capture());

        Form updatedForm = captor.getValue();

        assertThat(updatedForm.getTitle())
                .isEqualTo("Updated Household Form");

        assertThat(updatedForm.getCategory())
                .isEqualTo(FormCategory.EXTRA);

        assertThat(updatedForm.getTarget())
                .isEqualTo(FormTarget.HOUSEHOLD);

        assertThat(updatedForm.getDescription())
                .isEqualTo("Updated description");

        assertThat(updatedForm.isActive())
                .isTrue();
    }

    @Test
    void shouldNotUpdatePublishedForm() {
        UpdateFormRequest request = new UpdateFormRequest(
                "Updated Form",
                FormCategory.EXTRA,
                FormTarget.HOUSEHOLD,
                "Updated description",
                true
        );

        when(formRepository.findById(2L))
                .thenReturn(Optional.of(publishedForm));

        assertThatThrownBy(() ->
                formService.updateForm(2L, request)
        )
                .isInstanceOf(FormLockedException.class)
                .hasMessage(
                        "Form 'individual' is published and can no longer be edited. " +
                                "Create a new form to iterate on it."
                );

        verify(formRepository).findById(2L);

        verify(formRepository, never())
                .save(any(Form.class));
    }

    @Test
    void shouldThrowWhenUpdatingNonexistentForm() {
        UpdateFormRequest request = new UpdateFormRequest(
                "Updated Form",
                FormCategory.EXTRA,
                FormTarget.HOUSEHOLD,
                "Updated description",
                true
        );

        when(formRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                formService.updateForm(99L, request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Form not found: 99");

        verify(formRepository).findById(99L);

        verify(formRepository, never())
                .save(any(Form.class));
    }

    // =========================================================================
    // deleteForm()
    // =========================================================================

    @Test
    void shouldDeleteDraftForm() {
        when(formRepository.findById(1L))
                .thenReturn(Optional.of(draftForm));

        formService.deleteForm(1L);

        verify(formRepository).findById(1L);
        verify(formRepository).delete(draftForm);
    }

    @Test
    void shouldNotDeletePublishedForm() {
        when(formRepository.findById(2L))
                .thenReturn(Optional.of(publishedForm));

        assertThatThrownBy(() ->
                formService.deleteForm(2L)
        )
                .isInstanceOf(FormLockedException.class)
                .hasMessage(
                        "Form 'individual' is published and can no longer be edited. " +
                                "Create a new form to iterate on it."
                );

        verify(formRepository).findById(2L);

        verify(formRepository, never())
                .delete(any(Form.class));
    }

    @Test
    void shouldThrowWhenDeletingNonexistentForm() {
        when(formRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                formService.deleteForm(99L)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Form not found: 99");

        verify(formRepository).findById(99L);

        verify(formRepository, never())
                .delete(any(Form.class));
    }

    // =========================================================================
    // publishForm()
    // =========================================================================

    @Test
    void shouldPublishDraftForm() {
        when(formRepository.findById(1L))
                .thenReturn(Optional.of(draftForm));

        when(formRepository.save(any(Form.class)))
                .thenReturn(draftForm);

        FormDto result =
                formService.publishForm(1L);

        assertThat(result).isNotNull();

        ArgumentCaptor<Form> captor =
                ArgumentCaptor.forClass(Form.class);

        verify(formRepository).save(captor.capture());

        Form published = captor.getValue();

        assertThat(published.getStatus())
                .isEqualTo(FormStatus.PUBLISHED);
    }

    @Test
    void shouldRejectAlreadyPublishedForm() {
        when(formRepository.findById(2L))
                .thenReturn(Optional.of(publishedForm));

        assertThatThrownBy(() ->
                formService.publishForm(2L)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Form 'individual' is already published"
                );

        verify(formRepository).findById(2L);

        verify(formRepository, never())
                .save(any(Form.class));
    }

    @Test
    void shouldThrowWhenPublishingNonexistentForm() {
        when(formRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                formService.publishForm(99L)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Form not found: 99");

        verify(formRepository).findById(99L);

        verify(formRepository, never())
                .save(any(Form.class));
    }

    // =========================================================================
    // setActive()
    // =========================================================================

    @Test
    void shouldActivateForm() {
        when(formRepository.findById(1L))
                .thenReturn(Optional.of(draftForm));

        when(formRepository.save(any(Form.class)))
                .thenReturn(draftForm);

        formService.setActive(1L, true);

        ArgumentCaptor<Form> captor =
                ArgumentCaptor.forClass(Form.class);

        verify(formRepository).save(captor.capture());

        assertThat(captor.getValue().isActive())
                .isTrue();
    }

    @Test
    void shouldDeactivatePublishedForm() {
        when(formRepository.findById(2L))
                .thenReturn(Optional.of(publishedForm));

        when(formRepository.save(any(Form.class)))
                .thenReturn(publishedForm);

        formService.setActive(2L, false);

        ArgumentCaptor<Form> captor =
                ArgumentCaptor.forClass(Form.class);

        verify(formRepository).save(captor.capture());

        Form updated = captor.getValue();

        assertThat(updated.isActive())
                .isFalse();

        // Publishing does not prevent changing active state.
        assertThat(updated.getStatus())
                .isEqualTo(FormStatus.PUBLISHED);
    }

    @Test
    void shouldThrowWhenActivatingNonexistentForm() {
        when(formRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                formService.setActive(99L, true)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Form not found: 99");

        verify(formRepository).findById(99L);

        verify(formRepository, never())
                .save(any(Form.class));
    }

    // =========================================================================
    // assertEditable()
    // =========================================================================

    @Test
    void shouldAllowEditingDraftForm() {
        assertThatCode(
                () -> formService.assertEditable(draftForm)
        ).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectEditingPublishedForm() {
        assertThatThrownBy(() ->
                formService.assertEditable(publishedForm)
        )
                .isInstanceOf(FormLockedException.class)
                .hasMessage(
                        "Form 'individual' is published and can no longer be edited. " +
                                "Create a new form to iterate on it."
                );
    }
}