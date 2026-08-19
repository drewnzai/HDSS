package com.andrew.hdss.repositories;

import com.andrew.hdss.models.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByClientId(String clientId);
    Optional<Membership> findByIndividualIdAndEndDateIsNull(Long individualId);
    List<Membership> findByHouseholdIdInAndUpdatedAtAfter(List<Long> householdIds, Instant since);
    List<Membership> findByHouseholdIdIn(List<Long> householdIds);
}
