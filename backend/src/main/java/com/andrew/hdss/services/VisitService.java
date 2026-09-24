package com.andrew.hdss.services;

import com.andrew.hdss.dtos.CreateVisitRequest;
import com.andrew.hdss.dtos.VisitDto;
import com.andrew.hdss.exceptions.EntityNotFoundException;
import com.andrew.hdss.models.Household;
import com.andrew.hdss.models.Individual;
import com.andrew.hdss.models.User;
import com.andrew.hdss.models.Visit;
import com.andrew.hdss.models.enums.VisitStatus;
import com.andrew.hdss.repositories.HouseholdRepository;
import com.andrew.hdss.repositories.IndividualRepository;
import com.andrew.hdss.repositories.VisitRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final HouseholdRepository householdRepository;
    private final IndividualRepository individualRepository;
    private final AuthService authService;

    @Transactional
    public VisitDto createVisit(CreateVisitRequest request) {
        if (request.householdId() == null && request.individualId() == null) {
            throw new IllegalArgumentException(
                    "A visit must reference a household, an individual, or both"
            );
        }

        if (visitRepository.findByClientId(request.clientId()).isPresent()) {
            throw new IllegalArgumentException(
                    "A visit with clientId '" + request.clientId() + "' already exists"
            );
        }

        User conductedBy = authService.getCurrentUser();

        Visit visit = new Visit();
        visit.setClientId(request.clientId());
        visit.setVisitDate(request.visitDate());
        visit.setConductedBy(conductedBy);
        visit.setStatus(VisitStatus.IN_PROGRESS);

        if (request.householdId() != null) {
            Household household = householdRepository.findById(request.householdId())
                    .orElseThrow(() -> new EntityNotFoundException("Household not found: " + request.householdId()));
            visit.setHousehold(household);
        }

        return VisitDto.from(visitRepository.save(visit));
    }

    @Transactional(readOnly = true)
    public List<VisitDto> getAllVisits() {
        return visitRepository.findAll().stream().map(VisitDto::from).toList();
    }

    @Transactional(readOnly = true)
    public VisitDto getVisit(Long id) {
        return VisitDto.from(findVisitOrThrow(id));
    }

    @Transactional
    public VisitDto updateStatus(Long id, VisitStatus status) {
        Visit visit = findVisitOrThrow(id);
        visit.setStatus(status);
        return VisitDto.from(visitRepository.save(visit));
    }

    protected Visit findVisitOrThrow(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Visit not found: " + id));
    }
}
