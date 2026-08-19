package com.andrew.hdss.repositories;

import com.andrew.hdss.models.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {
    Optional<Household> findByClientId(String clientId);
    List<Household> findByLocationIdInAndUpdatedAtAfter(List<Long> locationIds, Instant since);
}
