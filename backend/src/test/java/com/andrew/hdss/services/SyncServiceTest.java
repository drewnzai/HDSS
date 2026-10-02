package com.andrew.hdss.services;

import com.andrew.hdss.dtos.AnswerPushDto;
import com.andrew.hdss.dtos.FormResponsePushDto;
import com.andrew.hdss.dtos.VisitPushDto;
import com.andrew.hdss.dtos.sync.*;
import com.andrew.hdss.models.*;
import com.andrew.hdss.models.enums.HouseholdStatus;
import com.andrew.hdss.models.enums.RelationshipToHead;
import com.andrew.hdss.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private IndividualRepository individualRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private FormResponseRepository formResponseRepository;

    @Mock
    private FormRepository formRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private SyncService syncService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setUsername("test-user");
    }

    // =========================================================================
    // PUSH
    // =========================================================================

    @Test
    void shouldPushNewHousehold() {
        Location location = new Location();
        location.setId(10L);
        location.setName("Village");

        when(authService.getCurrentUser()).thenReturn(currentUser);

        HouseholdPushDto dto = mock(HouseholdPushDto.class);

        when(dto.clientId()).thenReturn("household-client-1");
        when(dto.householdCode()).thenReturn("HH001");
        when(dto.locationId()).thenReturn(10L);
        when(dto.latitude()).thenReturn(null);
        when(dto.longitude()).thenReturn(null);

        Household saved = new Household();
        saved.setId(100L);

        when(householdRepository.findByClientId("household-client-1"))
                .thenReturn(Optional.empty());
        when(locationRepository.findById(10L))
                .thenReturn(Optional.of(location));
        when(locationRepository.findByParentId(10L))
                .thenReturn(List.of());
        when(householdRepository.save(any(Household.class)))
                .thenReturn(saved);

        SyncPushRequest request = mock(SyncPushRequest.class);

        when(request.households()).thenReturn(List.of(dto));
        when(request.individuals()).thenReturn(List.of());
        when(request.memberships()).thenReturn(List.of());
        when(request.visits()).thenReturn(List.of());
        when(request.formResponses()).thenReturn(List.of());
        when(request.answers()).thenReturn(List.of());

        SyncPushResponse response = syncService.push(request);

        assertThat(response).isNotNull();
        assertThat(response.households()).hasSize(1);

        ArgumentCaptor<Household> captor =
                ArgumentCaptor.forClass(Household.class);

        verify(householdRepository).save(captor.capture());

        Household household = captor.getValue();

        assertThat(household.getClientId())
                .isEqualTo("household-client-1");

        assertThat(household.getHouseholdCode())
                .isEqualTo("HH001");

        assertThat(household.getLocation())
                .isSameAs(location);

        assertThat(household.getCreatedBy())
                .isSameAs(currentUser);

        assertThat(household.getCreatedAt())
                .isNotNull();

        assertThat(household.getUpdatedAt())
                .isNotNull();

        assertThat(household.getStatus())
                .isEqualTo(HouseholdStatus.NOT_VIABLE);
    }

    @Test
    void shouldRejectHouseholdWhenLocationHasChildren() {
        Location location = new Location();
        location.setId(10L);
        location.setName("Village");

        Location child = new Location();
        child.setId(11L);

        HouseholdPushDto dto = mock(HouseholdPushDto.class);

        when(dto.clientId()).thenReturn("household-client-1");
        when(dto.locationId()).thenReturn(10L);

        when(householdRepository.findByClientId("household-client-1"))
                .thenReturn(Optional.empty());

        when(locationRepository.findById(10L))
                .thenReturn(Optional.of(location));

        when(locationRepository.findByParentId(10L))
                .thenReturn(List.of(child));

        SyncPushRequest request = mock(SyncPushRequest.class);

        when(request.households()).thenReturn(List.of(dto));
        when(request.individuals()).thenReturn(List.of());
        when(request.memberships()).thenReturn(List.of());
        when(request.visits()).thenReturn(List.of());
        when(request.formResponses()).thenReturn(List.of());
        when(request.answers()).thenReturn(List.of());

        SyncPushResponse response = syncService.push(request);

        assertThat(response.households()).hasSize(1);

        verify(householdRepository, never()).save(any());
    }

    @Test
    void shouldPushNewIndividualsAndResolveParents() {
        IndividualPushDto motherDto = mock(IndividualPushDto.class);
        IndividualPushDto childDto = mock(IndividualPushDto.class);

        when(motherDto.clientId()).thenReturn("mother-client");
        when(motherDto.extendedId()).thenReturn("M001");
        when(motherDto.firstName()).thenReturn("Jane");
        when(motherDto.lastName()).thenReturn("Doe");
        when(motherDto.sex()).thenReturn(null);
        when(motherDto.dateOfBirth()).thenReturn(null);
        when(motherDto.dobEstimated()).thenReturn(false);
        when(motherDto.motherClientId()).thenReturn(null);
        when(motherDto.fatherClientId()).thenReturn(null);

        when(childDto.clientId()).thenReturn("child-client");
        when(childDto.extendedId()).thenReturn("C001");
        when(childDto.firstName()).thenReturn("John");
        when(childDto.lastName()).thenReturn("Doe");
        when(childDto.sex()).thenReturn(null);
        when(childDto.dateOfBirth()).thenReturn(null);
        when(childDto.dobEstimated()).thenReturn(false);
        when(childDto.motherClientId()).thenReturn("mother-client");
        when(childDto.fatherClientId()).thenReturn(null);

        Individual mother = new Individual();
        mother.setId(1L);

        Individual child = new Individual();
        child.setId(2L);

        when(individualRepository.findByClientId("mother-client"))
                .thenReturn(Optional.empty());

        when(individualRepository.findByClientId("child-client"))
                .thenReturn(Optional.empty());

        when(individualRepository.save(any(Individual.class)))
                .thenAnswer(invocation -> {
                    Individual individual = invocation.getArgument(0);

                    if ("mother-client".equals(individual.getClientId())) {
                        return mother;
                    }

                    return child;
                });

        SyncPushRequest request = mock(SyncPushRequest.class);

        when(request.households()).thenReturn(List.of());
        when(request.individuals()).thenReturn(List.of(motherDto, childDto));
        when(request.memberships()).thenReturn(List.of());
        when(request.visits()).thenReturn(List.of());
        when(request.formResponses()).thenReturn(List.of());
        when(request.answers()).thenReturn(List.of());

        SyncPushResponse response = syncService.push(request);

        assertThat(response.individuals()).hasSize(2);

        assertThat(child.getMother())
                .isSameAs(mother);

        verify(individualRepository, times(2))
                .save(any(Individual.class));
    }

    @Test
    void shouldCreateNewMembership() {
        when(authService.getCurrentUser()).thenReturn(currentUser);

        Household household = new Household();
        household.setId(100L);

        Individual individual = new Individual();
        individual.setId(200L);
        individual.setExtendedId("IND001");

        HouseholdPushDto householdDto = mock(HouseholdPushDto.class);

        when(householdDto.clientId()).thenReturn("hh-client");
        when(householdDto.locationId()).thenReturn(10L);

        IndividualPushDto individualDto = mock(IndividualPushDto.class);

        when(individualDto.clientId()).thenReturn("ind-client");
        when(individualDto.extendedId()).thenReturn("IND001");
        when(individualDto.motherClientId()).thenReturn(null);
        when(individualDto.fatherClientId()).thenReturn(null);

        MembershipPushDto membershipDto = mock(MembershipPushDto.class);

        when(membershipDto.clientId()).thenReturn("membership-client");
        when(membershipDto.householdClientId()).thenReturn("hh-client");
        when(membershipDto.individualClientId()).thenReturn("ind-client");
        when(membershipDto.endDate()).thenReturn(null);
        when(membershipDto.relationshipToHead()).thenReturn(null);
        when(membershipDto.endType()).thenReturn(null);
        when(membershipDto.startDate()).thenReturn(null);
        when(membershipDto.startType()).thenReturn(null);

        Location location = new Location();
        location.setId(10L);

        when(householdRepository.findByClientId("hh-client"))
                .thenReturn(Optional.empty());

        when(locationRepository.findById(10L))
                .thenReturn(Optional.of(location));

        when(locationRepository.findByParentId(10L))
                .thenReturn(List.of());

        when(householdRepository.save(any(Household.class)))
                .thenReturn(household);

        when(individualRepository.findByClientId("ind-client"))
                .thenReturn(Optional.empty());

        when(individualRepository.save(any(Individual.class)))
                .thenReturn(individual);

        when(membershipRepository.findByClientId("membership-client"))
                .thenReturn(Optional.empty());

        when(membershipRepository
                .findByIndividualIdAndHouseholdIdAndEndDateIsNull(200L, 100L))
                .thenReturn(Optional.empty());

        when(membershipRepository
                .findByIndividualIdAndEndDateIsNull(200L))
                .thenReturn(Optional.empty());

        Membership savedMembership = new Membership();
        savedMembership.setId(300L);

        when(membershipRepository.save(any(Membership.class)))
                .thenReturn(savedMembership);

        SyncPushRequest request = mock(SyncPushRequest.class);

        when(request.households()).thenReturn(List.of(householdDto));
        when(request.individuals()).thenReturn(List.of(individualDto));
        when(request.memberships()).thenReturn(List.of(membershipDto));
        when(request.visits()).thenReturn(List.of());
        when(request.formResponses()).thenReturn(List.of());
        when(request.answers()).thenReturn(List.of());

        SyncPushResponse response = syncService.push(request);

        assertThat(response.memberships()).hasSize(1);

        ArgumentCaptor<Membership> captor =
                ArgumentCaptor.forClass(Membership.class);

        verify(membershipRepository).save(captor.capture());

        Membership membership = captor.getValue();

        assertThat(membership.getClientId())
                .isEqualTo("membership-client");

        assertThat(membership.getIndividual())
                .isSameAs(individual);

        assertThat(membership.getHousehold())
                .isSameAs(household);

        assertThat(membership.getCreatedBy())
                .isSameAs(currentUser);

        assertThat(membership.getCreatedAt())
                .isNotNull();
    }

    @Test
    void shouldRejectMembershipWhenIndividualHasOpenMembershipElsewhere() {
        Household household = new Household();
        household.setId(100L);

        Individual individual = new Individual();
        individual.setId(200L);
        individual.setExtendedId("IND001");

        HouseholdPushDto householdDto = mock(HouseholdPushDto.class);

        when(householdDto.clientId()).thenReturn("hh-client");
        when(householdDto.locationId()).thenReturn(10L);

        IndividualPushDto individualDto = mock(IndividualPushDto.class);

        when(individualDto.clientId()).thenReturn("ind-client");
        when(individualDto.extendedId()).thenReturn("IND001");
        when(individualDto.motherClientId()).thenReturn(null);
        when(individualDto.fatherClientId()).thenReturn(null);

        MembershipPushDto membershipDto = mock(MembershipPushDto.class);

        when(membershipDto.clientId()).thenReturn("membership-client");
        when(membershipDto.householdClientId()).thenReturn("hh-client");
        when(membershipDto.individualClientId()).thenReturn("ind-client");
        when(membershipDto.endDate()).thenReturn(null);

        Location location = new Location();
        location.setId(10L);

        when(householdRepository.findByClientId("hh-client"))
                .thenReturn(Optional.empty());

        when(locationRepository.findById(10L))
                .thenReturn(Optional.of(location));

        when(locationRepository.findByParentId(10L))
                .thenReturn(List.of());

        when(householdRepository.save(any(Household.class)))
                .thenReturn(household);

        when(individualRepository.findByClientId("ind-client"))
                .thenReturn(Optional.empty());

        when(individualRepository.save(any(Individual.class)))
                .thenReturn(individual);

        when(membershipRepository.findByClientId("membership-client"))
                .thenReturn(Optional.empty());

        when(membershipRepository
                .findByIndividualIdAndHouseholdIdAndEndDateIsNull(200L, 100L))
                .thenReturn(Optional.empty());

        Membership existingElsewhere = new Membership();
        existingElsewhere.setId(999L);

        when(membershipRepository
                .findByIndividualIdAndEndDateIsNull(200L))
                .thenReturn(Optional.of(existingElsewhere));

        SyncPushRequest request = mock(SyncPushRequest.class);

        when(request.households()).thenReturn(List.of(householdDto));
        when(request.individuals()).thenReturn(List.of(individualDto));
        when(request.memberships()).thenReturn(List.of(membershipDto));
        when(request.visits()).thenReturn(List.of());
        when(request.formResponses()).thenReturn(List.of());
        when(request.answers()).thenReturn(List.of());

        SyncPushResponse response = syncService.push(request);

        assertThat(response.memberships()).hasSize(1);

        verify(membershipRepository, never())
                .save(any(Membership.class));
    }

    @Test
    void shouldApplyHeadOfHouseholdRuleWhenOpeningHeadMembership() {
        Household household = new Household();
        household.setId(100L);

        Individual individual = new Individual();
        individual.setId(200L);

        MembershipPushDto membershipDto = mock(MembershipPushDto.class);

        when(membershipDto.clientId()).thenReturn("membership-client");
        when(membershipDto.householdClientId()).thenReturn("hh-client");
        when(membershipDto.individualClientId()).thenReturn("ind-client");
        when(membershipDto.endDate()).thenReturn(null);
        when(membershipDto.relationshipToHead())
                .thenReturn(RelationshipToHead.HEAD);
        when(membershipDto.endType()).thenReturn(null);
        when(membershipDto.startDate()).thenReturn(null);
        when(membershipDto.startType()).thenReturn(null);

        when(membershipRepository.findByClientId("membership-client"))
                .thenReturn(Optional.empty());

        when(membershipRepository
                .findByIndividualIdAndHouseholdIdAndEndDateIsNull(200L, 100L))
                .thenReturn(Optional.empty());

        when(membershipRepository
                .findByIndividualIdAndEndDateIsNull(200L))
                .thenReturn(Optional.empty());

        when(membershipRepository.save(any(Membership.class)))
                .thenAnswer(invocation -> {
                    Membership membership = invocation.getArgument(0);
                    membership.setId(300L);
                    return membership;
                });

        when(householdRepository.findByClientId("hh-client"))
                .thenReturn(Optional.of(household));

        when(individualRepository.findByClientId("ind-client"))
                .thenReturn(Optional.of(individual));

        SyncPushRequest request = mock(SyncPushRequest.class);

        when(request.households()).thenReturn(List.of());
        when(request.individuals()).thenReturn(List.of());
        when(request.memberships()).thenReturn(List.of(membershipDto));
        when(request.visits()).thenReturn(List.of());
        when(request.formResponses()).thenReturn(List.of());
        when(request.answers()).thenReturn(List.of());

        syncService.push(request);

        assertThat(household.getHead())
                .isSameAs(individual);

        assertThat(household.getStatus())
                .isEqualTo(HouseholdStatus.ACTIVE);

        verify(householdRepository).save(household);
    }

    @Test
    void shouldPushNewVisitAndResolveBatchReferences() {
        Household household = new Household();
        household.setId(100L);

        when(authService.getCurrentUser()).thenReturn(currentUser);

        Individual individual = new Individual();
        individual.setId(200L);

        HouseholdPushDto householdDto = mock(HouseholdPushDto.class);

        when(householdDto.clientId()).thenReturn("hh-client");
        when(householdDto.locationId()).thenReturn(10L);

        IndividualPushDto individualDto = mock(IndividualPushDto.class);

        when(individualDto.clientId()).thenReturn("ind-client");
        when(individualDto.motherClientId()).thenReturn(null);
        when(individualDto.fatherClientId()).thenReturn(null);

        VisitPushDto visitDto = mock(VisitPushDto.class);

        when(visitDto.clientId()).thenReturn("visit-client");
        when(visitDto.householdClientId()).thenReturn("hh-client");
        when(visitDto.individualClientId()).thenReturn("ind-client");
        when(visitDto.status()).thenReturn(null);
        when(visitDto.visitDate()).thenReturn(null);

        Location location = new Location();
        location.setId(10L);

        when(householdRepository.findByClientId("hh-client"))
                .thenReturn(Optional.empty());

        when(locationRepository.findById(10L))
                .thenReturn(Optional.of(location));

        when(locationRepository.findByParentId(10L))
                .thenReturn(List.of());

        when(householdRepository.save(any(Household.class)))
                .thenReturn(household);

        when(individualRepository.findByClientId("ind-client"))
                .thenReturn(Optional.empty());

        when(individualRepository.save(any(Individual.class)))
                .thenReturn(individual);

        when(visitRepository.findByClientId("visit-client"))
                .thenReturn(Optional.empty());

        Visit savedVisit = new Visit();
        savedVisit.setId(500L);

        when(visitRepository.save(any(Visit.class)))
                .thenReturn(savedVisit);

        SyncPushRequest request = mock(SyncPushRequest.class);

        when(request.households()).thenReturn(List.of(householdDto));
        when(request.individuals()).thenReturn(List.of(individualDto));
        when(request.memberships()).thenReturn(List.of());
        when(request.visits()).thenReturn(List.of(visitDto));
        when(request.formResponses()).thenReturn(List.of());
        when(request.answers()).thenReturn(List.of());

        SyncPushResponse response = syncService.push(request);

        assertThat(response.visitResults())
                .hasSize(1);

        ArgumentCaptor<Visit> captor =
                ArgumentCaptor.forClass(Visit.class);

        verify(visitRepository).save(captor.capture());

        Visit visit = captor.getValue();

        assertThat(visit.getClientId())
                .isEqualTo("visit-client");

        assertThat(visit.getHousehold())
                .isSameAs(household);

        assertThat(visit.getIndividual())
                .isSameAs(individual);

        assertThat(visit.getConductedBy())
                .isSameAs(currentUser);
    }

    @Test
    void shouldRejectVisitWithoutHouseholdOrIndividual() {
        VisitPushDto visitDto = mock(VisitPushDto.class);

        when(visitDto.clientId()).thenReturn("visit-client");
        when(visitDto.householdClientId()).thenReturn(null);
        when(visitDto.individualClientId()).thenReturn(null);

        SyncPushRequest request = mock(SyncPushRequest.class);

        when(request.households()).thenReturn(List.of());
        when(request.individuals()).thenReturn(List.of());
        when(request.memberships()).thenReturn(List.of());
        when(request.visits()).thenReturn(List.of(visitDto));
        when(request.formResponses()).thenReturn(List.of());
        when(request.answers()).thenReturn(List.of());

        SyncPushResponse response = syncService.push(request);

        assertThat(response.visitResults())
                .hasSize(1);

        verify(visitRepository, never())
                .save(any());
    }

    @Test
    void shouldCreateFormResponseAndAnswerUsingBatchReferences() {
        Visit visit = new Visit();
        visit.setId(500L);

        Form form = new Form();
        form.setId(600L);

        Question question = new Question();
        question.setId(700L);

        Individual individual = new Individual();
        individual.setId(200L);

        IndividualPushDto individualDto = mock(IndividualPushDto.class);

        when(individualDto.clientId()).thenReturn("ind-client");
        when(individualDto.motherClientId()).thenReturn(null);
        when(individualDto.fatherClientId()).thenReturn(null);

        when(individualRepository.findByClientId("ind-client"))
                .thenReturn(Optional.empty());

        when(individualRepository.save(any(Individual.class)))
                .thenReturn(individual);

        VisitPushDto visitDto = mock(VisitPushDto.class);

        when(visitDto.clientId()).thenReturn("visit-client");
        when(visitDto.householdClientId()).thenReturn(null);
        when(visitDto.individualClientId()).thenReturn("ind-client");
        when(visitDto.status()).thenReturn(null);
        when(visitDto.visitDate()).thenReturn(null);

        when(visitRepository.findByClientId("visit-client"))
                .thenReturn(Optional.empty());

        when(visitRepository.save(any(Visit.class)))
                .thenReturn(visit);

        FormResponsePushDto formResponseDto =
                mock(FormResponsePushDto.class);

        when(formResponseDto.clientId())
                .thenReturn("response-client");

        when(formResponseDto.visitClientId())
                .thenReturn("visit-client");

        when(formResponseDto.formId())
                .thenReturn(600L);

        when(formResponseDto.status())
                .thenReturn(null);

        when(formResponseDto.completedAt())
                .thenReturn(null);

        when(formResponseDto.formVersion())
                .thenReturn(1);

        when(formResponseDto.startedAt())
                .thenReturn(null);

        when(formResponseRepository.findByClientId("response-client"))
                .thenReturn(Optional.empty());

        when(formRepository.findById(600L))
                .thenReturn(Optional.of(form));

        FormResponse formResponse = new FormResponse();
        formResponse.setId(800L);

        when(formResponseRepository.save(any(FormResponse.class)))
                .thenReturn(formResponse);

        AnswerPushDto answerDto = mock(AnswerPushDto.class);

        when(answerDto.clientId())
                .thenReturn("answer-client");

        when(answerDto.formResponseClientId())
                .thenReturn("response-client");

        when(answerDto.questionId())
                .thenReturn(700L);

        when(answerDto.value())
                .thenReturn("Yes");

        when(answerRepository.findByClientId("answer-client"))
                .thenReturn(Optional.empty());

        when(questionRepository.findById(700L))
                .thenReturn(Optional.of(question));

        Answer answer = new Answer();
        answer.setId(900L);

        when(answerRepository.save(any(Answer.class)))
                .thenReturn(answer);

        SyncPushRequest request = mock(SyncPushRequest.class);

        when(request.households()).thenReturn(List.of());
        when(request.individuals()).thenReturn(List.of(individualDto));
        when(request.memberships()).thenReturn(List.of());
        when(request.visits()).thenReturn(List.of(visitDto));
        when(request.formResponses()).thenReturn(List.of(formResponseDto));
        when(request.answers()).thenReturn(List.of(answerDto));

        SyncPushResponse response = syncService.push(request);

        assertThat(response.formResponseResults())
                .hasSize(1);

        assertThat(response.answerResults())
                .hasSize(1);

        ArgumentCaptor<FormResponse> formResponseCaptor =
                ArgumentCaptor.forClass(FormResponse.class);

        verify(formResponseRepository)
                .save(formResponseCaptor.capture());

        FormResponse capturedResponse =
                formResponseCaptor.getValue();

        assertThat(capturedResponse.getClientId())
                .isEqualTo("response-client");

        assertThat(capturedResponse.getVisit())
                .isSameAs(visit);

        assertThat(capturedResponse.getForm())
                .isSameAs(form);

        assertThat(capturedResponse.getFormVersion())
                .isEqualTo(1);

        ArgumentCaptor<Answer> answerCaptor =
                ArgumentCaptor.forClass(Answer.class);

        verify(answerRepository)
                .save(answerCaptor.capture());

        Answer capturedAnswer =
                answerCaptor.getValue();

        assertThat(capturedAnswer.getClientId())
                .isEqualTo("answer-client");

        assertThat(capturedAnswer.getValue())
                .isEqualTo("Yes");

        assertThat(capturedAnswer.getFormResponse())
                .isSameAs(formResponse);

        assertThat(capturedAnswer.getQuestion())
                .isSameAs(question);
    }

    // =========================================================================
    // UPDATE EXISTING RECORDS
    // =========================================================================

    @Test
    void shouldUpdateExistingHouseholdWithoutSettingCreationFields() {
        Household existing = new Household();

        existing.setId(100L);
        existing.setClientId("household-client");
        existing.setCreatedBy(currentUser);
        existing.setCreatedAt(
                Instant.now().minusSeconds(1000)
        );

        Location location = new Location();
        location.setId(10L);
        location.setName("Village");

        HouseholdPushDto dto = mock(HouseholdPushDto.class);

        when(dto.clientId()).thenReturn("household-client");
        when(dto.householdCode()).thenReturn("HH002");
        when(dto.locationId()).thenReturn(10L);
        when(dto.latitude()).thenReturn(null);
        when(dto.longitude()).thenReturn(null);

        when(householdRepository.findByClientId("household-client"))
                .thenReturn(Optional.of(existing));

        when(locationRepository.findById(10L))
                .thenReturn(Optional.of(location));

        when(locationRepository.findByParentId(10L))
                .thenReturn(List.of());

        when(householdRepository.save(any(Household.class)))
                .thenReturn(existing);

        SyncPushRequest request = mock(SyncPushRequest.class);

        when(request.households()).thenReturn(List.of(dto));
        when(request.individuals()).thenReturn(List.of());
        when(request.memberships()).thenReturn(List.of());
        when(request.visits()).thenReturn(List.of());
        when(request.formResponses()).thenReturn(List.of());
        when(request.answers()).thenReturn(List.of());

        syncService.push(request);

        assertThat(existing.getHouseholdCode())
                .isEqualTo("HH002");

        assertThat(existing.getCreatedBy())
                .isSameAs(currentUser);

        verify(householdRepository).save(existing);
    }

    @Test
    void shouldUpdateExistingVisitWithoutOverwritingImmutableFields() {
        Household household = new Household();
        household.setId(100L);

        Individual individual = new Individual();
        individual.setId(200L);

        Visit existing = new Visit();

        existing.setId(500L);
        existing.setClientId("visit-client");
        existing.setHousehold(household);
        existing.setIndividual(individual);

        VisitPushDto dto = mock(VisitPushDto.class);

        when(dto.clientId()).thenReturn("visit-client");
        when(dto.householdClientId()).thenReturn("other-household");
        when(dto.status()).thenReturn(null);

        when(visitRepository.findByClientId("visit-client"))
                .thenReturn(Optional.of(existing));

        when(visitRepository.save(any(Visit.class)))
                .thenReturn(existing);

        SyncPushRequest request = mock(SyncPushRequest.class);

        when(request.households()).thenReturn(List.of());
        when(request.individuals()).thenReturn(List.of());
        when(request.memberships()).thenReturn(List.of());
        when(request.visits()).thenReturn(List.of(dto));
        when(request.formResponses()).thenReturn(List.of());
        when(request.answers()).thenReturn(List.of());

        syncService.push(request);

        assertThat(existing.getHousehold())
                .isSameAs(household);

        assertThat(existing.getIndividual())
                .isSameAs(individual);

        verify(visitRepository).save(existing);
    }

    // =========================================================================
    // ERROR ISOLATION
    // =========================================================================

    @Test
    void shouldContinueProcessingOtherHouseholdsWhenOneFails() {
        HouseholdPushDto badDto = mock(HouseholdPushDto.class);
        HouseholdPushDto goodDto = mock(HouseholdPushDto.class);

        when(badDto.clientId()).thenReturn("bad");
        when(badDto.locationId()).thenReturn(999L);

        when(goodDto.clientId()).thenReturn("good");
        when(goodDto.locationId()).thenReturn(10L);
        when(goodDto.householdCode()).thenReturn("HH001");
        when(goodDto.latitude()).thenReturn(null);
        when(goodDto.longitude()).thenReturn(null);

        Location location = new Location();
        location.setId(10L);

        Household saved = new Household();
        saved.setId(100L);

        when(householdRepository.findByClientId("bad"))
                .thenReturn(Optional.empty());

        when(locationRepository.findById(999L))
                .thenReturn(Optional.empty());

        when(householdRepository.findByClientId("good"))
                .thenReturn(Optional.empty());

        when(locationRepository.findById(10L))
                .thenReturn(Optional.of(location));

        when(locationRepository.findByParentId(10L))
                .thenReturn(List.of());

        when(householdRepository.save(any(Household.class)))
                .thenReturn(saved);

        SyncPushRequest request = mock(SyncPushRequest.class);

        when(request.households())
                .thenReturn(List.of(badDto, goodDto));

        when(request.individuals()).thenReturn(List.of());
        when(request.memberships()).thenReturn(List.of());
        when(request.visits()).thenReturn(List.of());
        when(request.formResponses()).thenReturn(List.of());
        when(request.answers()).thenReturn(List.of());

        SyncPushResponse response = syncService.push(request);

        assertThat(response.households())
                .hasSize(2);

        verify(householdRepository)
                .save(any(Household.class));
    }

    // =========================================================================
    // PULL
    // =========================================================================

    @Test
    void shouldPullDataForLocationAndDescendants() {
        Long locationId = 10L;

        Instant since =
                Instant.parse("2026-09-01T00:00:00Z");

        Location root = new Location();

        root.setId(10L);
        root.setAncestorPath("");

        Location child1 = new Location();
        child1.setId(20L);

        Location child2 = new Location();
        child2.setId(30L);

        when(locationRepository.findById(10L))
                .thenReturn(Optional.of(root));

        when(locationRepository.findAllDescendants("10/"))
                .thenReturn(List.of(child1, child2));

        Household household = new Household();
        household.setId(100L);

        when(householdRepository
                .findByLocationIdInAndUpdatedAtAfter(
                        eq(List.of(20L, 30L, 10L)),
                        eq(since)
                ))
                .thenReturn(List.of(household));

        Membership membership = new Membership();

        when(membershipRepository
                .findByHouseholdIdInAndUpdatedAtAfter(
                        eq(List.of(100L)),
                        eq(since)
                ))
                .thenReturn(List.of(membership));

        Individual individual = new Individual();
        individual.setId(200L);

        membership.setIndividual(individual);

        when(membershipRepository
                .findByHouseholdIdIn(eq(List.of(100L))))
                .thenReturn(List.of(membership));

        when(individualRepository
                .findByIdInAndUpdatedAtAfter(
                        eq(List.of(200L)),
                        eq(since)
                ))
                .thenReturn(List.of(individual));

        SyncPullResponse response =
                syncService.pull(locationId, since);

        assertThat(response)
                .isNotNull();

        assertThat(response.syncedAt())
                .isNotNull();

        assertThat(response.households())
                .containsExactly(household);

        assertThat(response.individuals())
                .containsExactly(individual);

        assertThat(response.memberships())
                .containsExactly(membership);

        verify(locationRepository)
                .findAllDescendants("10/");

        verify(householdRepository)
                .findByLocationIdInAndUpdatedAtAfter(
                        List.of(20L, 30L, 10L),
                        since
                );

        verify(membershipRepository)
                .findByHouseholdIdInAndUpdatedAtAfter(
                        List.of(100L),
                        since
                );

        verify(individualRepository)
                .findByIdInAndUpdatedAtAfter(
                        List.of(200L),
                        since
                );
    }

    @Test
    void shouldThrowWhenPullLocationDoesNotExist() {
        when(locationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                syncService.pull(
                        999L,
                        Instant.parse("2026-09-01T00:00:00Z")
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Location not found: 999");

        verify(locationRepository, never())
                .findAllDescendants(anyString());

        verifyNoInteractions(householdRepository);
        verifyNoInteractions(membershipRepository);
        verifyNoInteractions(individualRepository);
    }
}