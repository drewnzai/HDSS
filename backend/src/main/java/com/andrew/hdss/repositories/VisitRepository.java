package com.andrew.hdss.repositories;

import com.andrew.hdss.models.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    Optional<Visit> findByClientId(String clientId);
    List<Visit> findByHouseholdId(Long householdId);
}
