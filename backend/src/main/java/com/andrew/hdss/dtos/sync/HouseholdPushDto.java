package com.andrew.hdss.dtos.sync;

public record HouseholdPushDto(
        String clientId,
        String householdCode,
        Long locationId,
        Double latitude,
        Double longitude
) {}
