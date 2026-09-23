package com.andrew.hdss.dtos;

public record CreateChoiceRequest(
        String listName,
        String name,
        String label,
        Integer orderIndex
) {
}
