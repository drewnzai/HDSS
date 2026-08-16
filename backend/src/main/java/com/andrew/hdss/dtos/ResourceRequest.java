package com.andrew.hdss.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceRequest {
    private Integer page = 0;
    private Integer size = 10;
}
