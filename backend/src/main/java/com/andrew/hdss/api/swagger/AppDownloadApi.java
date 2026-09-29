package com.andrew.hdss.api.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface AppDownloadApi {
    @Operation(summary = "Download HDSS APK")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "APK downloaded successfully"
                    )
            }
    )
    ResponseEntity<Resource> downloadApk();
}
