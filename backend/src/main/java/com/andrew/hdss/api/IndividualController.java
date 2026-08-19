package com.andrew.hdss.api;

import com.andrew.hdss.api.swaggerinterfaces.IndividualApi;
import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.IndividualDto;
import com.andrew.hdss.dtos.ResourceRequest;
import com.andrew.hdss.services.IndividualService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/individuals")
@RequiredArgsConstructor
@Tag(name = "Individual Data Retrieval")
public class IndividualController implements IndividualApi {

    private final IndividualService individualService;

    @GetMapping
    public BatchResponse<IndividualDto> getIndividuals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return individualService.getIndividuals(new ResourceRequest(page, size));
    }
}