package com.andrew.hdss.api.swaggerinterfaces;

import com.andrew.hdss.dtos.LoginRequest;
import com.andrew.hdss.dtos.LoginResponse;
import com.andrew.hdss.dtos.RefreshTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthApi {

    @Operation(
            summary = "Verify a new account",
            description = "Verifies a user's verification token to enable their account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful account verification")
    })
    ResponseEntity<String> verifyAccount(@PathVariable String token);

    @Operation(
            summary = "Login",
            description = "Authenticate a user's credentials"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful login")
    })
    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest);

    @Operation(
            summary = "Refresh JWT",
            description = "Refreshes a user's JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful refresh")
    })
    ResponseEntity<LoginResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest);
}