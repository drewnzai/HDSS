package com.andrew.hdss.api.swaggerinterfaces;

import com.andrew.hdss.dtos.CreateFormRequest;
import com.andrew.hdss.dtos.FormDto;
import com.andrew.hdss.dtos.UpdateFormRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface FormApi {

    @Operation(summary = "Get all forms")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Forms retrieved successfully"
                    )
            }
    )
    List<FormDto> getAllForms();

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
            description = "Can only update a form if the form has not received form responses yet"
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
            summary = "Delete a form",
            description = "Can only delete a form if the form has not received form responses yet"
    )
    void deleteForm(@PathVariable Long id);
}
