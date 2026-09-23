package com.andrew.hdss.dtos;

public record ChoiceDto(
        Long id,
        String listName,
        String name,
        String label,
        Integer orderIndex
) {
}
