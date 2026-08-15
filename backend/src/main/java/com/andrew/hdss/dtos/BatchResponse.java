package com.andrew.hdss.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponse<T> {
    private Integer size;
    private Integer page;
    private Long totalElements;
    private Integer totalPages;
    private List<T> data;
}
