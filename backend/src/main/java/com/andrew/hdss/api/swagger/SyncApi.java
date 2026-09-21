package com.andrew.hdss.api.swagger;

import com.andrew.hdss.dtos.sync.SyncPullResponse;
import com.andrew.hdss.dtos.sync.SyncPushRequest;
import com.andrew.hdss.dtos.sync.SyncPushResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;

public interface SyncApi {
    @Operation(
            summary = "Data Push",
            description = "Create or update HDSS data"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful Push")
    })
    SyncPushResponse push(@RequestBody SyncPushRequest request);

    @Operation(
            summary = "Data Pull",
            description = "Download HDSS data"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful Pull")
    })
    SyncPullResponse pull(
            @RequestParam Long locationId,
            @RequestParam(required = false) Instant since
    );
}
