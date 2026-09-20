package com.andrew.hdss.services;

import com.andrew.hdss.dtos.CreateFormRequest;
import com.andrew.hdss.dtos.FormDto;
import com.andrew.hdss.dtos.UpdateFormRequest;
import com.andrew.hdss.exceptions.EntityNotFoundException;
import com.andrew.hdss.exceptions.FormLockedException;
import com.andrew.hdss.exceptions.ResourceAlreadyExistsException;
import com.andrew.hdss.models.Form;
import com.andrew.hdss.repositories.FormRepository;
import com.andrew.hdss.repositories.FormResponseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class FormService {

    private final FormRepository formRepository;
    private final FormResponseRepository formResponseRepository;

    @Transactional(readOnly = true)
    public List<FormDto> getAllForms() {
        return formRepository.findAll().stream()
                .map(form -> FormDto.from(form, isLocked(form.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public FormDto getForm(Long id) {
        Form form = findFormOrThrow(id);
        return FormDto.from(form, isLocked(id));
    }

    @Transactional
    public FormDto createForm(CreateFormRequest request) {
        if (formRepository.findByName(request.name()).isPresent()) {
            throw new ResourceAlreadyExistsException("A form named '" + request.name() + "' already exists");
        }

        Form form = new Form();
        form.setName(request.name());
        form.setTitle(request.title());
        form.setCategory(request.category());
        form.setTarget(request.target());
        form.setDescription(request.description());
        form.setVersion(1);
        form.setActive(true);

        Form saved = formRepository.save(form);
        return FormDto.from(saved, false);
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

        Form saved = formRepository.save(form);
        return FormDto.from(saved, false); // assertEditable already confirmed this
    }

    @Transactional
    public void deleteForm(Long id) {
        Form form = findFormOrThrow(id);
        assertEditable(form);
        formRepository.delete(form);
    }

    @Transactional(readOnly = true)
    protected boolean isLocked(Long formId) {
        return formResponseRepository.existsByFormId(formId);
    }

    public void assertEditable(Form form) {
        if (isLocked(form.getId())) {
            throw new FormLockedException(
                    "Form '" + form.getName() + "' has live responses and can no longer be edited. " +
                            "Create a new form to iterate on it."
            );
        }
    }

    private Form findFormOrThrow(Long id) {
        return formRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form not found: " + id));
    }
}
