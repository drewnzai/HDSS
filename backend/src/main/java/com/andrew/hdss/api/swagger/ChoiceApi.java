package com.andrew.hdss.api.swagger;

import com.andrew.hdss.dtos.ChoiceDto;
import com.andrew.hdss.dtos.CreateChoiceRequest;
import com.andrew.hdss.dtos.UpdateChoiceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ChoiceApi {
    @Operation(summary = "Get listNames")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List names retrieved successfully"
                    )
            }
    )
    List<String> getListNames();

    @Operation(summary = "Get Choices by listName")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Choices retrieved successfully"
                    )
            }
    )
    List<ChoiceDto> getByListName(@PathVariable String listName);

    @Operation(summary = "Create a Choice")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Choice created successfully"
                    )
            }
    )
    ChoiceDto create(@RequestBody CreateChoiceRequest request);

    @Operation(summary = "Update a Choice")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Choice updated successfully"
                    )
            }
    )
    ChoiceDto update(@PathVariable Long id, @RequestBody UpdateChoiceRequest request);

    @Operation(summary = "Delete a Choice")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Choice deleted successfully"
                    )
            }
    )
    ResponseEntity<String> delete(@PathVariable Long id);
}
