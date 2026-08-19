package com.andrew.hdss.services;

import com.andrew.hdss.dtos.sync.*;
import com.andrew.hdss.exceptions.EntityNotFoundException;
import com.andrew.hdss.models.*;
import com.andrew.hdss.models.enums.HouseholdStatus;
import com.andrew.hdss.models.enums.RelationshipToHead;
import com.andrew.hdss.repositories.HouseholdRepository;
import com.andrew.hdss.repositories.IndividualRepository;
import com.andrew.hdss.repositories.LocationRepository;
import com.andrew.hdss.repositories.MembershipRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SyncService {
    private final HouseholdRepository householdRepository;
    private final IndividualRepository individualRepository;
    private final MembershipRepository membershipRepository;
    private final LocationRepository locationRepository;
    private final AuthService authService;

    @Transactional
    public SyncPushResponse push(SyncPushRequest request) {
        User currentUser = authService.getCurrentUser();

        // Households first — nothing else can resolve without them.
        Map<String, Household> householdsByClientId = new HashMap<>();
        List<SyncItemResult> householdResults = new ArrayList<>();
        for (HouseholdPushDto dto : request.households()) {
            try {
                Household household = upsertHousehold(dto, currentUser);
                householdsByClientId.put(dto.clientId(), household);
                householdResults.add(household.getId() == null
                        ? SyncItemResult.created(dto.clientId(), null)
                        : SyncItemResult.updated(dto.clientId(), household.getId()));
            } catch (Exception e) {
                householdResults.add(SyncItemResult.error(dto.clientId(), e.getMessage()));
            }
        }

        // Individuals — two passes so mother/father can reference a sibling
        // in the same batch regardless of array order.
        Map<String, Individual> individualsByClientId = new HashMap<>();
        List<SyncItemResult> individualResults = new ArrayList<>();
        for (IndividualPushDto dto : request.individuals()) {
            try {
                Individual individual = upsertIndividualShell(dto, currentUser);
                individualsByClientId.put(dto.clientId(), individual);
                individualResults.add(individual.getId() == null
                        ? SyncItemResult.created(dto.clientId(), null)
                        : SyncItemResult.updated(dto.clientId(), individual.getId()));
            } catch (Exception e) {
                individualResults.add(SyncItemResult.error(dto.clientId(), e.getMessage()));
            }
        }
        for (IndividualPushDto dto : request.individuals()) {
            resolveParents(dto, individualsByClientId);
        }

        // Memberships last — depends on both maps above being fully populated.
        List<SyncItemResult> membershipResults = new ArrayList<>();
        for (MembershipPushDto dto : request.memberships()) {
            try {
                Membership membership = upsertMembership(dto, householdsByClientId, individualsByClientId, currentUser);
                membershipResults.add(membership.getId() == null
                        ? SyncItemResult.created(dto.clientId(), null)
                        : SyncItemResult.updated(dto.clientId(), membership.getId()));
            } catch (Exception e) {
                membershipResults.add(SyncItemResult.error(dto.clientId(), e.getMessage()));
            }
        }

        return new SyncPushResponse(householdResults, individualResults, membershipResults);
    }

    private Household upsertHousehold(HouseholdPushDto dto, User currentUser) {
        Household household = householdRepository.findByClientId(dto.clientId())
                .orElseGet(Household::new);

        boolean isNew = household.getId() == null;

        Location location = locationRepository.findById(dto.locationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + dto.locationId()));

        if (!locationRepository.findByParentId(location.getId()).isEmpty()) {
            throw new IllegalArgumentException(
                    "Household location must be a leaf — '" + location.getName() + "' has child locations."
            );
        }

        household.setClientId(dto.clientId());
        household.setHouseholdCode(dto.householdCode());
        household.setLocation(location);
        household.setLatitude(dto.latitude());
        household.setLongitude(dto.longitude());
        household.setUpdatedAt(Instant.now());

        if (isNew) {
            household.setCreatedBy(currentUser);
            household.setCreatedAt(Instant.now());
            household.setStatus(HouseholdStatus.NOT_VIABLE); // no head assigned yet
        }

        return householdRepository.save(household);
    }

    // ---------- Individuals ----------

    private Individual upsertIndividualShell(IndividualPushDto dto, User currentUser) {
        Individual individual = individualRepository.findByClientId(dto.clientId())
                .orElseGet(Individual::new);

        boolean isNew = individual.getId() == null;

        individual.setClientId(dto.clientId());
        individual.setExtendedId(dto.extendedId());
        individual.setFirstName(dto.firstName());
        individual.setLastName(dto.lastName());
        individual.setSex(dto.sex());
        individual.setDateOfBirth(dto.dateOfBirth());
        individual.setDobEstimated(dto.dobEstimated());
        individual.setUpdatedAt(Instant.now());

        if (isNew) {
            individual.setCreatedBy(currentUser);
            individual.setCreatedAt(Instant.now());
        }

        return individualRepository.save(individual);
    }

    private void resolveParents(IndividualPushDto dto, Map<String, Individual> individualsByClientId) {
        Individual individual = individualsByClientId.get(dto.clientId());

        if (dto.motherClientId() != null) {
            Individual mother = resolveIndividual(dto.motherClientId(), individualsByClientId);
            individual.setMother(mother);
        }
        if (dto.fatherClientId() != null) {
            Individual father = resolveIndividual(dto.fatherClientId(), individualsByClientId);
            individual.setFather(father);
        }
        individualRepository.save(individual);
    }

    // ---------- Memberships ----------

    private Membership upsertMembership(
            MembershipPushDto dto,
            Map<String, Household> householdsByClientId,
            Map<String, Individual> individualsByClientId,
            User currentUser
    ) {
        Membership membership = membershipRepository.findByClientId(dto.clientId())
                .orElseGet(Membership::new);

        boolean isNew = membership.getId() == null;

        Individual individual = resolveIndividual(dto.individualClientId(), individualsByClientId);
        Household household = resolveHousehold(dto.householdClientId(), householdsByClientId);

        // Enforce: an individual has at most one OPEN membership at a time.
        if (isNew && dto.endDate() == null) {
            membershipRepository.findByIndividualIdAndEndDateIsNull(individual.getId())
                    .ifPresent(existingOpen -> {
                        throw new IllegalStateException(
                                "Individual " + individual.getExtendedId() +
                                        " already has an open membership (id " + existingOpen.getId() +
                                        "). Close it before opening a new one."
                        );
                    });
        }

        membership.setClientId(dto.clientId());
        membership.setIndividual(individual);
        membership.setHousehold(household);
        membership.setRelationshipToHead(dto.relationshipToHead());
        membership.setStartDate(dto.startDate());
        membership.setStartType(dto.startType());
        membership.setEndDate(dto.endDate());
        membership.setEndType(dto.endType());
        membership.setUpdatedAt(Instant.now());

        if (isNew) {
            membership.setCreatedBy(currentUser);
            membership.setCreatedAt(Instant.now());
        }

        membership = membershipRepository.save(membership);

        applyHeadOfHouseholdRules(membership, household);

        return membership;
    }

    // A HEAD membership opening assigns the head and marks the household viable.
    // A HEAD membership closing without an immediate replacement marks it not-viable.
    private void applyHeadOfHouseholdRules(Membership membership, Household household) {
        if (membership.getRelationshipToHead() != RelationshipToHead.HEAD) {
            return;
        }

        if (membership.getEndDate() == null) {
            household.setHead(membership.getIndividual());
            household.setStatus(HouseholdStatus.ACTIVE);
        } else if (household.getHead() != null
                && household.getHead().getId().equals(membership.getIndividual().getId())) {
            household.setHead(null);
            household.setStatus(HouseholdStatus.NOT_VIABLE);
        }

        household.setUpdatedAt(Instant.now());
        householdRepository.save(household);
    }

    // ---------- Reference resolution: batch map first, DB fallback ----------

    private Individual resolveIndividual(String clientId, Map<String, Individual> batch) {
        Individual fromBatch = batch.get(clientId);
        if (fromBatch != null) return fromBatch;
        return individualRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown individual clientId: " + clientId));
    }

    private Household resolveHousehold(String clientId, Map<String, Household> batch) {
        Household fromBatch = batch.get(clientId);
        if (fromBatch != null) return fromBatch;
        return householdRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown household clientId: " + clientId));
    }

    // ---------- Pull ----------

    @Transactional(readOnly = true)
    public SyncPullResponse pull(Long locationId, Instant since) {
        Instant syncStartedAt = Instant.now();

        List<Long> locationIds = locationRepository.findAllDescendants(
                        buildAncestorPrefix(locationId)
                ).stream()
                .map(Location::getId)
                .toList();
        // include the scoping location itself, not just its descendants
        List<Long> scopedLocationIds = new ArrayList<>(locationIds);
        scopedLocationIds.add(locationId);

        List<Household> households = householdRepository
                .findByLocationIdInAndUpdatedAtAfter(scopedLocationIds, since);

        List<Long> householdIds = households.stream().map(Household::getId).toList();

        // Memberships changed since last sync, scoped to these households
        List<Membership> changedMemberships = membershipRepository
                .findByHouseholdIdInAndUpdatedAtAfter(householdIds, since);

        // Every individual ever linked to a household in scope, so a
        // standalone individual edit (e.g. a name correction with no
        // membership change) is still picked up.
        List<Long> allIndividualIdsInScope = membershipRepository
                .findByHouseholdIdIn(householdIds).stream()
                .map(m -> m.getIndividual().getId())
                .distinct()
                .toList();

        List<Individual> changedIndividuals = individualRepository
                .findByIdInAndUpdatedAtAfter(allIndividualIdsInScope, since);

        return new SyncPullResponse(syncStartedAt, households, changedIndividuals, changedMemberships);
    }

    private String buildAncestorPrefix(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));
        return location.getAncestorPath() + location.getId() + "/";
    }
}
