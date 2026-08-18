package com.andrew.hdss.services;

import com.andrew.hdss.repositories.IndividualRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IndividualService {
    private final IndividualRepository individualRepository;
}
