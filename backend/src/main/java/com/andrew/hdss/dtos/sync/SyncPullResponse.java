package com.andrew.hdss.dtos.sync;

import com.andrew.hdss.models.Household;
import com.andrew.hdss.models.Individual;
import com.andrew.hdss.models.Membership;

import java.time.Instant;
import java.util.List;

public record SyncPullResponse(
        Instant syncedAt,
        List<Household> households,
        List<Individual> individuals,
        List<Membership> memberships
) {}