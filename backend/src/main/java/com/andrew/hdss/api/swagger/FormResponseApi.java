package com.andrew.hdss.api.swagger;

import com.andrew.hdss.dtos.CreateFormResponseRequest;
import com.andrew.hdss.dtos.FormResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface FormResponseApi {

    @Operation(summary = "Start a form response")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Form response recorded successfully"
                    )
            }
    )
    FormResponseDto startFormResponse(
            @PathVariable Long visitId,
            @RequestBody CreateFormResponseRequest request
    );

    @Operation(summary = "Get form responses for a specific visit")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Form responses retrieved successfully"
                    )
            }
    )
    List<FormResponseDto> getResponsesForVisit(@PathVariable Long visitId);

    @Operation(summary = "Get form response")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Form response retrieved successfully"
                    )
            }
    )
    FormResponseDto getFormResponse(@PathVariable Long id);

    @Operation(summary = "Complete a form response")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Form response completed successfully"
                    )
            }
    )
    FormResponseDto completeFormResponse(@PathVariable Long id);
}
