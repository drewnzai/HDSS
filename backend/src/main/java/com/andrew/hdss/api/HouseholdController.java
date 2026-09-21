package com.andrew.hdss.api;

import com.andrew.hdss.api.swagger.HouseholdApi;
import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.HouseholdDto;
import com.andrew.hdss.dtos.ResourceRequest;
import com.andrew.hdss.services.HouseholdService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/households")
@RequiredArgsConstructor
@Tag(name = "Household Data Retrieval")
public class HouseholdController implements HouseholdApi {

    private final HouseholdService householdService;

    @GetMapping
    public BatchResponse<HouseholdDto> getHouseholds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return householdService.getHouseholds(new ResourceRequest(page, size));
    }
}
