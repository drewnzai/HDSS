package com.andrew.hdss.api.swaggerinterfaces;

import com.andrew.hdss.dtos.CreateLocationRequest;
import com.andrew.hdss.dtos.LocationDto;
import com.andrew.hdss.dtos.LocationImportResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LocationApi {
    @Operation(
            summary = "Location's Children",
            description = "Get a location's immediate children"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval")
    })
    List<LocationDto> getChildren(@RequestParam(name = "id", required = false) Long parentId);

    @Operation(
            summary = "Location's Descendants",
            description = "Get a location's descendants"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval")
    })
    List<LocationDto> getDescendants(@PathVariable(name = "id") Long id);

    @Operation(
            summary = "Location's Ancestors",
            description = "Get a location's ancestors"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval")
    })
    List<LocationDto> getAncestors(@PathVariable Long id);

    @Operation(
            summary = "Location Creation",
            description = "Create a location"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful creation")
    })
    ResponseEntity<String> createLocation(@RequestBody CreateLocationRequest request);

    @Operation(
            summary = "Location Population",
            description = "Populate locations from an Excel spreadsheet"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful population")
    })
    LocationImportResult importLocations(@RequestParam("file") MultipartFile file) throws IOException;
}
