package com.andrew.hdss.services;

import com.andrew.hdss.repositories.MembershipRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MembershipService {
    private final MembershipRepository membershipRepository;
}
