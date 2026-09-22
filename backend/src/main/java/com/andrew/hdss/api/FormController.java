package com.andrew.hdss.api;// package: match your existing controller package
// Update to the FormController given earlier — adds /publish and /active.

import com.andrew.hdss.dtos.CreateFormRequest;
import com.andrew.hdss.dtos.FormDto;
import com.andrew.hdss.dtos.SetFormActiveRequest;
import com.andrew.hdss.dtos.UpdateFormRequest;
import com.andrew.hdss.services.FormService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
@Tag(name = "Form Management")
public class FormController {

    private final FormService formService;

    @GetMapping
    public List<FormDto> getAllForms() {
        return formService.getAllForms();
    }

    @GetMapping("/{id}")
    public FormDto getForm(@PathVariable Long id) {
        return formService.getForm(id);
    }

    @PostMapping
    public FormDto createForm(@RequestBody CreateFormRequest request) {
        return formService.createForm(request);
    }

    @PutMapping("/{id}")
    public FormDto updateForm(@PathVariable Long id, @RequestBody UpdateFormRequest request) {
        return formService.updateForm(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteForm(@PathVariable Long id) {
        formService.deleteForm(id);
    }

    @PutMapping("/{id}/publish")
    public FormDto publishForm(@PathVariable Long id) {
        return formService.publishForm(id);
    }

    @PutMapping("/{id}/active")
    public FormDto setActive(@PathVariable Long id, @RequestBody SetFormActiveRequest request) {
        return formService.setActive(id, request.active());
    }
}
