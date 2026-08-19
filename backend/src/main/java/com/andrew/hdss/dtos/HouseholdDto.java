package com.andrew.hdss.dtos;

import com.andrew.hdss.models.Household;
import com.andrew.hdss.models.enums.HouseholdStatus;

public record HouseholdDto(
        Long id,
        String clientId,
        String householdCode,
        Long locationId,
        String locationName,
        Double latitude,
        Double longitude,
        Long headIndividualId,
        String headName,
        HouseholdStatus status
) {
    public static HouseholdDto from(Household h) {
        return new HouseholdDto(
                h.getId(),
                h.getClientId(),
                h.getHouseholdCode(),
                h.getLocation() != null ? h.getLocation().getId() : null,
                h.getLocation() != null ? h.getLocation().getName() : null,
                h.getLatitude(),
                h.getLongitude(),
                h.getHead() != null ? h.getHead().getId() : null,
                h.getHead() != null ? h.getHead().getFirstName() + " " + h.getHead().getLastName() : null,
                h.getStatus()
        );
    }
}
