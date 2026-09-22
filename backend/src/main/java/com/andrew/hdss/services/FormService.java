package com.andrew.hdss.services;

import com.andrew.hdss.dtos.CreateFormRequest;
import com.andrew.hdss.dtos.FormDto;
import com.andrew.hdss.dtos.UpdateFormRequest;
import com.andrew.hdss.exceptions.FormLockedException;
import com.andrew.hdss.models.Form;
import com.andrew.hdss.models.enums.FormStatus;
import com.andrew.hdss.repositories.FormRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class FormService {

    private final FormRepository formRepository;

    @Transactional(readOnly = true)
    public List<FormDto> getAllForms() {
        return formRepository.findAll().stream().map(FormDto::from).toList();
    }

    @Transactional(readOnly = true)
    public FormDto getForm(Long id) {
        return FormDto.from(findFormOrThrow(id));
    }

    @Transactional
    public FormDto createForm(CreateFormRequest request) {
        if (formRepository.findByName(request.name()).isPresent()) {
            throw new IllegalArgumentException("A form named '" + request.name() + "' already exists");
        }

        Form form = new Form();
        form.setName(request.name());
        form.setTitle(request.title());
        form.setCategory(request.category());
        form.setTarget(request.target());
        form.setDescription(request.description());
        form.setVersion(1);
        form.setActive(false);
        form.setStatus(FormStatus.DRAFT);

        return FormDto.from(formRepository.save(form));
    }

    @Transactional
    public FormDto updateForm(Long id, UpdateFormRequest request) {
        Form form = findFormOrThrow(id);
        assertEditable(form);

        form.setTitle(request.title());
        form.setCategory(request.category());
        form.setTarget(request.target());
        form.setDescription(request.description());
        form.setActive(request.active());

        return FormDto.from(formRepository.save(form));
    }

    @Transactional
    public void deleteForm(Long id) {
        Form form = findFormOrThrow(id);
        assertEditable(form);
        formRepository.delete(form);
    }

    @Transactional
    public FormDto publishForm(Long id) {
        Form form = findFormOrThrow(id);
        if (form.getStatus() == FormStatus.PUBLISHED) {
            throw new IllegalStateException("Form '" + form.getName() + "' is already published");
        }
        form.setStatus(FormStatus.PUBLISHED);
        return FormDto.from(formRepository.save(form));
    }

    // Deliberately separate from updateForm/assertEditable — toggling
    // whether a form is currently offered doesn't touch its structure, so
    // it's allowed even after publishing (e.g. retiring an old form).
    @Transactional
    public FormDto setActive(Long id, boolean active) {
        Form form = findFormOrThrow(id);
        form.setActive(active);
        return FormDto.from(formRepository.save(form));
    }

    public void assertEditable(Form form) {
        if (form.getStatus() == FormStatus.PUBLISHED) {
            throw new FormLockedException(
                    "Form '" + form.getName() + "' is published and can no longer be edited. " +
                    "Create a new form to iterate on it."
            );
        }
    }

    private Form findFormOrThrow(Long id) {
        return formRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form not found: " + id));
    }
}
