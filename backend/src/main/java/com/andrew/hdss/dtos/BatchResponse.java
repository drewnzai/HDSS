package com.andrew.hdss.dtos;

import java.util.List;

public record BatchResponse<T>(
        Integer count,

        Long totalCount,

        String nextAfterId,

        List<T> data
) {

}
