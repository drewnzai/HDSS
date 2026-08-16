package com.andrew.hdss.api.swaggerinterfaces;

import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.UserCreationRequest;
import com.andrew.hdss.dtos.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserApi {

    @Operation(
            summary = "Get Users",
            description = "Get user information"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                    description = "Users retrieved successfully")
            }
    )
    ResponseEntity<BatchResponse<UserDto>> getUsers(@RequestParam(name = "page", required = false) Integer page,
                                                    @RequestParam(name = "size", required = false) Integer size);

    @Operation(
            summary = "User Creation",
            description = "Create a new user"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            description = "User created successfully")
            }
    )
    ResponseEntity<String> createUser(@Valid @RequestBody UserCreationRequest request) throws Exception;

    @Operation(
            summary = "User Deletion",
            description = "Delete an existing user"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",
                            description = "User deleted successfully")
            }
    )
    ResponseEntity<String> deleteUser(@Valid @RequestBody UserDto userDto) throws Exception;
}
