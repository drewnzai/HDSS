package com.andrew.hdss.dtos;

import com.andrew.hdss.round.ValidationReport;

public record RoundCreationResult(
        ValidationReport validationReport,
        RoundDto round
) {
    public boolean started() {
        return round != null;
    }
}