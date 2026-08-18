package com.andrew.hdss.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateLocationRequest {
    @NotBlank(message = "name is required")
    private String name;
    @NotBlank(message = "type is required")
    private String type;
    @NotBlank(message = "code is required")
    private String code;
    private Long parentId;
}
