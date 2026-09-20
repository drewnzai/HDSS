package com.andrew.hdss.dtos;

import com.andrew.hdss.models.enums.FormCategory;
import com.andrew.hdss.models.enums.FormTarget;

public record CreateFormRequest(
        String name,
        String title,
        FormCategory category,
        FormTarget target,
        String description
) {}
