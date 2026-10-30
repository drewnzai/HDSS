package com.andrew.hdss.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoundRequest(

        @NotBlank(message = "Round name is required")
        @Size(max = 100, message = "Round name cannot exceed 100 characters")
        String roundName

) {
}