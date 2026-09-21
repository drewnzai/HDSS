package com.andrew.hdss.api;

import com.andrew.hdss.api.swagger.FormResponseApi;
import com.andrew.hdss.dtos.CreateFormResponseRequest;
import com.andrew.hdss.dtos.FormResponseDto;
import com.andrew.hdss.services.FormResponseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Form Response Management")
public class FormResponseController implements FormResponseApi {

    private final FormResponseService formResponseService;

    @PostMapping("/api/visits/{visitId}/responses")
    public FormResponseDto startFormResponse(
            @PathVariable Long visitId,
            @RequestBody CreateFormResponseRequest request
    ) {
        return formResponseService.startFormResponse(visitId, request);
    }

    @GetMapping("/api/visits/{visitId}/responses")
    public List<FormResponseDto> getResponsesForVisit(@PathVariable Long visitId) {
        return formResponseService.getResponsesForVisit(visitId);
    }

    @GetMapping("/api/form-responses/{id}")
    public FormResponseDto getFormResponse(@PathVariable Long id) {
        return formResponseService.getFormResponse(id);
    }

    @PutMapping("/api/form-responses/{id}/complete")
    public FormResponseDto completeFormResponse(@PathVariable Long id) {
        return formResponseService.completeFormResponse(id);
    }
}
