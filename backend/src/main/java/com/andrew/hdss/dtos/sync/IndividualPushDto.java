package com.andrew.hdss.dtos.sync;

import com.andrew.hdss.models.enums.Sex;

import java.time.LocalDate;

public record IndividualPushDto(
        String clientId,
        String extendedId,
        String firstName,
        String lastName,
        Sex sex,
        LocalDate dateOfBirth,
        boolean dobEstimated,
        String motherClientId,
        String fatherClientId
) {}
