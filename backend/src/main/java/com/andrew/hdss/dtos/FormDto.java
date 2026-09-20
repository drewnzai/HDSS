package com.andrew.hdss.dtos;

import com.andrew.hdss.models.Form;
import com.andrew.hdss.models.enums.FormCategory;
import com.andrew.hdss.models.enums.FormTarget;

public record FormDto(
        Long id,
        String name,
        String title,
        FormCategory category,
        FormTarget target,
        Integer version,
        String description,
        boolean active,
        boolean locked // computed — does any FormResponse exist for this form?
) {
    public static FormDto from(Form form, boolean locked) {
        return new FormDto(
                form.getId(),
                form.getName(),
                form.getTitle(),
                form.getCategory(),
                form.getTarget(),
                form.getVersion(),
                form.getDescription(),
                form.isActive(),
                locked
        );
    }
}