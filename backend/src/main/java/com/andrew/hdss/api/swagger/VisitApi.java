package com.andrew.hdss.api.swagger;

import com.andrew.hdss.dtos.CreateVisitRequest;
import com.andrew.hdss.dtos.UpdateVisitStatusRequest;
import com.andrew.hdss.dtos.VisitDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface VisitApi {

    @Operation(summary = "Get visits")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Visits retrieved successfully"
                    )
            }
    )
    List<VisitDto> getAllVisits();

    @Operation(summary = "Get visit")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Visit retrieved successfully"
                    )
            }
    )
    VisitDto getVisit(@PathVariable Long id);

    @Operation(summary = "Create visit")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Visit created successfully"
                    )
            }
    )
    VisitDto createVisit(
            @RequestBody CreateVisitRequest request
    );

    @Operation(summary = "Update a visit's status")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Visit status updated successfully"
                    )
            }
    )
    VisitDto updateStatus(@PathVariable Long id, @RequestBody UpdateVisitStatusRequest request);
}
