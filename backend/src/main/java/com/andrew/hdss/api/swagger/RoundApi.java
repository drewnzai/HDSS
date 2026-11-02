package com.andrew.hdss.api.swagger;

import com.andrew.hdss.dtos.CreateRoundRequest;
import com.andrew.hdss.dtos.RoundDetailsDto;
import com.andrew.hdss.dtos.RoundDto;
import com.andrew.hdss.round.ValidationReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface RoundApi {
    @Operation(summary = "Validate live DB data")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Data validated successfully"
                    )
            }
    )
    ResponseEntity<ValidationReport> validate();

    @Operation(summary = "Create a round")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Round created successfully"
                    )
            }
    )
    ResponseEntity<?> createRound(
            @Valid @RequestBody CreateRoundRequest request
    );

    @Operation(summary = "Get rounds")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Rounds retrieved successfully"
                    )
            }
    )
    ResponseEntity<List<RoundDto>> getRounds();

    @Operation(summary = "Get a round")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Round retrieved successfully"
                    )
            }
    )
    ResponseEntity<RoundDetailsDto> getRound(
            @PathVariable Long id
    );
}
