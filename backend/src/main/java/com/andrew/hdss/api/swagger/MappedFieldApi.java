package com.andrew.hdss.api.swagger;

import com.andrew.hdss.dtos.MappableFieldDto;
import com.andrew.hdss.models.enums.MappedEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface MappedFieldApi {
    @Operation(summary = "Get mapped field list by entity")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Mapped field list retrieved successfully"
                    )
            }
    )
    List<MappableFieldDto> getMappableFields(@PathVariable MappedEntity entity);
}
