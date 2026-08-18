package com.andrew.hdss.services;

import com.andrew.hdss.repositories.HouseholdRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class HouseholdService {
    private final HouseholdRepository householdRepository;
}
