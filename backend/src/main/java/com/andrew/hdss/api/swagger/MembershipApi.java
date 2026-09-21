package com.andrew.hdss.api.swagger;

import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.MembershipDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.RequestParam;

public interface MembershipApi {

    @Operation(
            summary = "Get Memberships",
            description = "Get memberships' information"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            description = "Memberships retrieved successfully")
            }
    )
    BatchResponse<MembershipDto> getMemberships(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );
}
