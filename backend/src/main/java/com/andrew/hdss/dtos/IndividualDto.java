package com.andrew.hdss.dtos;

import com.andrew.hdss.models.Individual;
import com.andrew.hdss.models.enums.Sex;

import java.time.LocalDate;

public record IndividualDto(
        Long id,
        String clientId,
        String extendedId,
        String firstName,
        String lastName,
        Sex sex,
        LocalDate dateOfBirth,
        boolean dobEstimated,
        Long motherId,
        Long fatherId
) {
    public static IndividualDto from(Individual i) {
        return new IndividualDto(
                i.getId(),
                i.getClientId(),
                i.getExtendedId(),
                i.getFirstName(),
                i.getLastName(),
                i.getSex(),
                i.getDateOfBirth(),
                i.isDobEstimated(),
                i.getMother() != null ? i.getMother().getId() : null,
                i.getFather() != null ? i.getFather().getId() : null
        );
    }
}