package com.andrew.hdss.services;

import com.andrew.hdss.dtos.AnswerPushDto;
import com.andrew.hdss.dtos.FormResponsePushDto;
import com.andrew.hdss.dtos.VisitPushDto;
import com.andrew.hdss.dtos.sync.*;
import com.andrew.hdss.models.*;
import com.andrew.hdss.models.enums.HouseholdStatus;
import com.andrew.hdss.models.enums.RelationshipToHead;
import com.andrew.hdss.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class SyncService {
    private final HouseholdRepository householdRepository;
    private final IndividualRepository individualRepository;
    private final MembershipRepository membershipRepository;
    private final LocationRepository locationRepository;
    private final VisitRepository visitRepository;
    private final FormResponseRepository formResponseRepository;
    private final FormRepository formRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AuthService authService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "households", allEntries = true),
            @CacheEvict(cacheNames = "individuals", allEntries = true),
            @CacheEvict(cacheNames = "memberships", allEntries = true)
    })
    public SyncPushResponse push(SyncPushRequest request) {
        User currentUser = authService.getCurrentUser();

        Map<String, Household> householdsByClientId = new HashMap<>();
        List<SyncItemResult> householdResults = new ArrayList<>();
        for (HouseholdPushDto dto : request.households()) {
            try {
                UpsertResult<Household> result = upsertHousehold(dto, currentUser);
                householdsByClientId.put(dto.clientId(), result.entity());
                householdResults.add(result.wasNew()
                        ? SyncItemResult.created(dto.clientId(), result.entity().getId())
                        : SyncItemResult.updated(dto.clientId(), result.entity().getId()));
            } catch (Exception e) {
                householdResults.add(SyncItemResult.error(dto.clientId(), e.getMessage()));
            }
        }

        Map<String, Individual> individualsByClientId = new HashMap<>();
        List<SyncItemResult> individualResults = new ArrayList<>();
        for (IndividualPushDto dto : request.individuals()) {
            try {
                UpsertResult<Individual> result = upsertIndividualShell(dto, currentUser);
                individualsByClientId.put(dto.clientId(), result.entity());
                individualResults.add(result.wasNew()
                        ? SyncItemResult.created(dto.clientId(), result.entity().getId())
                        : SyncItemResult.updated(dto.clientId(), result.entity().getId()));
            } catch (Exception e) {
                individualResults.add(SyncItemResult.error(dto.clientId(), e.getMessage()));
            }
        }
        for (IndividualPushDto dto : request.individuals()) {
            resolveParents(dto, individualsByClientId);
        }

        List<SyncItemResult> membershipResults = new ArrayList<>();
        for (MembershipPushDto dto : request.memberships()) {
            try {
                UpsertResult<Membership> result =
                        upsertMembership(dto, householdsByClientId, individualsByClientId, currentUser);
                membershipResults.add(result.wasNew()
                        ? SyncItemResult.created(dto.clientId(), result.entity().getId())
                        : SyncItemResult.updated(dto.clientId(), result.entity().getId()));
            } catch (Exception e) {
                membershipResults.add(SyncItemResult.error(dto.clientId(), e.getMessage()));
            }
        }

        Map<String, Visit> visitsByClientId = new HashMap<>();
        List<SyncItemResult> visitResults = new ArrayList<>();
        for (VisitPushDto dto : request.visits()) {
            try {
                UpsertResult<Visit> result =
                        upsertVisit(dto, householdsByClientId, individualsByClientId, currentUser);
                visitsByClientId.put(dto.clientId(), result.entity());
                visitResults.add(result.wasNew()
                        ? SyncItemResult.created(dto.clientId(), result.entity().getId())
                        : SyncItemResult.updated(dto.clientId(), result.entity().getId()));
            } catch (Exception e) {
                visitResults.add(SyncItemResult.error(dto.clientId(), e.getMessage()));
            }
        }

        Map<String, FormResponse> formResponsesByClientId = new HashMap<>();
        List<SyncItemResult> formResponseResults = new ArrayList<>();
        for (FormResponsePushDto dto : request.formResponses()) {
            try {
                UpsertResult<FormResponse> result = upsertFormResponse(dto, visitsByClientId);
                formResponsesByClientId.put(dto.clientId(), result.entity());
                formResponseResults.add(result.wasNew()
                        ? SyncItemResult.created(dto.clientId(), result.entity().getId())
                        : SyncItemResult.updated(dto.clientId(), result.entity().getId()));
            } catch (Exception e) {
                formResponseResults.add(SyncItemResult.error(dto.clientId(), e.getMessage()));
            }
        }

        List<SyncItemResult> answerResults = new ArrayList<>();
        for (AnswerPushDto dto : request.answers()) {
            try {
                UpsertResult<Answer> result = upsertAnswer(dto, formResponsesByClientId);
                answerResults.add(result.wasNew()
                        ? SyncItemResult.created(dto.clientId(), result.entity().getId())
                        : SyncItemResult.updated(dto.clientId(), result.entity().getId()));
            } catch (Exception e) {
                answerResults.add(SyncItemResult.error(dto.clientId(), e.getMessage()));
            }
        }

        return new SyncPushResponse(
                householdResults, individualResults, membershipResults,
                visitResults, formResponseResults, answerResults
        );
    }

    private UpsertResult<Household> upsertHousehold(HouseholdPushDto dto, User currentUser) {
        Household household = householdRepository.findByClientId(dto.clientId())
                .orElseGet(Household::new);

        boolean isNew = household.getId() == null;

        Location location = locationRepository.findById(dto.locationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + dto.locationId()));

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
            household.setStatus(HouseholdStatus.NOT_VIABLE);
        }

        Household saved = householdRepository.save(household);
        return new UpsertResult<>(saved, isNew);
    }

    // ---------- Individuals ----------

    private UpsertResult<Individual> upsertIndividualShell(IndividualPushDto dto, User currentUser) {
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

        Individual saved = individualRepository.save(individual);
        return new UpsertResult<>(saved, isNew);
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

    private UpsertResult<Membership> upsertMembership(
            MembershipPushDto dto,
            Map<String, Household> householdsByClientId,
            Map<String, Individual> individualsByClientId,
            User currentUser
    ) {
        Optional<Membership> existingByClientId = membershipRepository.findByClientId(dto.clientId());
        Individual individual = resolveIndividual(dto.individualClientId(), individualsByClientId);
        Household household = resolveHousehold(dto.householdClientId(), householdsByClientId);

        Membership membership;
        boolean isNew;

        if (existingByClientId.isPresent()) {
            membership = existingByClientId.get();
            isNew = false;
        } else if (dto.endDate() == null) {
            Optional<Membership> openAtThisHousehold = membershipRepository
                    .findByIndividualIdAndHouseholdIdAndEndDateIsNull(individual.getId(), household.getId());

            if (openAtThisHousehold.isPresent()) {
                membership = openAtThisHousehold.get();
                isNew = false;
            } else {
                Optional<Membership> openElsewhere = membershipRepository
                        .findByIndividualIdAndEndDateIsNull(individual.getId());
                if (openElsewhere.isPresent()) {
                    throw new IllegalStateException(
                            "Individual " + individual.getExtendedId() +
                                    " already has an open membership at a different household (id " +
                                    openElsewhere.get().getId() + "). Close it before opening a new one."
                    );
                }
                membership = new Membership();
                isNew = true;
            }
        } else {
            membership = new Membership();
            isNew = true;
        }

        membership.setIndividual(individual);
        membership.setHousehold(household);
        membership.setRelationshipToHead(dto.relationshipToHead());
        membership.setEndDate(dto.endDate());
        membership.setEndType(dto.endType());
        membership.setUpdatedAt(Instant.now());

        if (isNew) {
            membership.setClientId(dto.clientId());
            membership.setStartDate(dto.startDate());
            membership.setStartType(dto.startType());
            membership.setCreatedBy(currentUser);
            membership.setCreatedAt(Instant.now());
        }

        Membership saved = membershipRepository.save(membership);
        applyHeadOfHouseholdRules(saved, household);

        return new UpsertResult<>(saved, isNew);
    }

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

    // ---------- Visits ----------

    private UpsertResult<Visit> upsertVisit(
            VisitPushDto dto,
            Map<String, Household> householdsByClientId,
            Map<String, Individual> individualsByClientId,
            User currentUser
    ) {
        if (dto.householdClientId() == null && dto.individualClientId() == null) {
            throw new IllegalArgumentException("A visit must reference a household, an individual, or both");
        }

        Visit visit = visitRepository.findByClientId(dto.clientId()).orElseGet(Visit::new);
        boolean isNew = visit.getId() == null;

        // status is the only field that legitimately changes post-creation
        // (IN_PROGRESS -> COMPLETED/ABANDONED) — household/individual/
        // visitDate/conductedBy are set once, at creation, same spirit as
        // Membership not overwriting clientId/startDate/startType on a
        // correction update.
        visit.setStatus(dto.status());
        visit.setUpdatedAt(LocalDateTime.now());

        if (isNew) {
            visit.setClientId(dto.clientId());
            visit.setVisitDate(dto.visitDate());
            visit.setConductedBy(currentUser);

            if (dto.householdClientId() != null) {
                visit.setHousehold(resolveHousehold(dto.householdClientId(), householdsByClientId));
            }

            if (dto.individualClientId() != null) {
                visit.setIndividual(resolveIndividual(dto.individualClientId(), individualsByClientId));
            }
        }

        Visit saved = visitRepository.save(visit);
        return new UpsertResult<>(saved, isNew);
    }

    // ---------- FormResponses ----------

    private UpsertResult<FormResponse> upsertFormResponse(
            FormResponsePushDto dto,
            Map<String, Visit> visitsByClientId
    ) {
        FormResponse formResponse = formResponseRepository.findByClientId(dto.clientId())
                .orElseGet(FormResponse::new);
        boolean isNew = formResponse.getId() == null;

        formResponse.setStatus(dto.status());
        formResponse.setCompletedAt(dto.completedAt());

        if (isNew) {
            Visit visit = resolveVisit(dto.visitClientId(), visitsByClientId);

            // Form/Question are synced-down reference data (no clientId —
            // Android already has the real server id from download), unlike
            // Visit above which is offline-created and resolved by clientId.
            Form form = formRepository.findById(dto.formId())
                    .orElseThrow(() -> new IllegalArgumentException("Form not found: " + dto.formId()));

            formResponse.setClientId(dto.clientId());
            formResponse.setVisit(visit);
            formResponse.setForm(form);
            // Trusted as stamped by Android at start-time — see
            // FormResponsePushDto's doc comment for why this is never
            // recomputed from form.getVersion() here.
            formResponse.setFormVersion(dto.formVersion());
            formResponse.setStartedAt(dto.startedAt());
        }

        FormResponse saved = formResponseRepository.save(formResponse);
        return new UpsertResult<>(saved, isNew);
    }

    // ---------- Answers ----------

    private UpsertResult<Answer> upsertAnswer(
            AnswerPushDto dto,
            Map<String, FormResponse> formResponsesByClientId
    ) {
        Answer answer = answerRepository.findByClientId(dto.clientId()).orElseGet(Answer::new);
        boolean isNew = answer.getId() == null;

        answer.setValue(dto.value());

        if (isNew) {
            FormResponse formResponse = resolveFormResponse(dto.formResponseClientId(), formResponsesByClientId);

            Question question = questionRepository.findById(dto.questionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found: " + dto.questionId()));

            answer.setClientId(dto.clientId());
            answer.setFormResponse(formResponse);
            answer.setQuestion(question);
        }

        Answer saved = answerRepository.save(answer);
        return new UpsertResult<>(saved, isNew);
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

    private Visit resolveVisit(String clientId, Map<String, Visit> batch) {
        Visit fromBatch = batch.get(clientId);
        if (fromBatch != null) return fromBatch;
        return visitRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown visit clientId: " + clientId));
    }

    private FormResponse resolveFormResponse(String clientId, Map<String, FormResponse> batch) {
        FormResponse fromBatch = batch.get(clientId);
        if (fromBatch != null) return fromBatch;
        return formResponseRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown form response clientId: " + clientId));
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
        List<Long> scopedLocationIds = new ArrayList<>(locationIds);
        scopedLocationIds.add(locationId);

        List<Household> households = householdRepository
                .findByLocationIdInAndUpdatedAtAfter(scopedLocationIds, since);

        List<Long> householdIds = households.stream().map(Household::getId).toList();

        List<Membership> changedMemberships = membershipRepository
                .findByHouseholdIdInAndUpdatedAtAfter(householdIds, since);

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
