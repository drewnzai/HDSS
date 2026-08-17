package com.andrew.hdss.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateLocationRequest {
    private String name;
    private String type;
    private Long parentId;
}
