package com.andrew.hdss.api.swaggerinterfaces;

import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.IndividualDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.RequestParam;

public interface IndividualApi {

    @Operation(
            summary = "Get Individuals",
            description = "Get individuals' information"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            description = "Individuals retrieved successfully")
            }
    )
    BatchResponse<IndividualDto> getIndividuals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );
}
