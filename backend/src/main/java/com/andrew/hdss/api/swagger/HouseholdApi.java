package com.andrew.hdss.api.swagger;

import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.HouseholdDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.RequestParam;

public interface HouseholdApi {

    @Operation(
            summary = "Get Households",
            description = "Get households' information"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            description = "Households retrieved successfully")
            }
    )
    BatchResponse<HouseholdDto> getHouseholds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );
}
