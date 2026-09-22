package com.andrew.hdss.api.swagger;

import com.andrew.hdss.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface FormApi {

    @Operation(summary = "Get all forms")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "All forms retrieved successfully"
                    )
            }
    )
    BatchResponse<FormDto> getAllForms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "Get an individual form")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Form retrieved successfully"
                    )
            }
    )
    FormDto getForm(@PathVariable Long id);

    @Operation(summary = "Create a form")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Form created successfully"
                    )
            }
    )
    FormDto createForm(@RequestBody CreateFormRequest request);

    @Operation(
            summary = "Update a form",
            description = "Can only update a form if the form has not been marked as published"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Form updated successfully"
                    )
            }
    )
    FormDto updateForm(@PathVariable Long id, @RequestBody UpdateFormRequest request);

    @Operation(
            summary = "Delete a form"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Form deleted successfully"
                    )
            }
    )
    ResponseEntity<String> deleteForm(@PathVariable Long id);

    @Operation(
            summary = "Mark a form as published"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Form marked as published successfully"
                    )
            }
    )
    FormDto publishForm(@PathVariable Long id);

    @Operation(
            summary = "Mark a form as active"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Form marked as active successfully"
                    )
            }
    )
    FormDto setActive(@PathVariable Long id, @RequestBody SetFormActiveRequest request);
}
