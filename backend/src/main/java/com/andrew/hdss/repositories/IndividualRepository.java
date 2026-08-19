package com.andrew.hdss.repositories;

import com.andrew.hdss.models.Individual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface IndividualRepository extends JpaRepository<Individual, Long> {
    Optional<Individual> findByClientId(String clientId);
    List<Individual> findByIdInAndUpdatedAtAfter(List<Long> ids, Instant since);
}
