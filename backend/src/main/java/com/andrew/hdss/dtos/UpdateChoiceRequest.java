package com.andrew.hdss.dtos;

public record UpdateChoiceRequest(
        String name,
        String label,
        Integer orderIndex
) {
}
