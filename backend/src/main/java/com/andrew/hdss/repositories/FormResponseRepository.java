package com.andrew.hdss.repositories;

import com.andrew.hdss.models.FormResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormResponseRepository extends JpaRepository<FormResponse, Long> {
    boolean existsByFormId(Long formId);
    Optional<FormResponse> findByClientId(String clientId);
    List<FormResponse> findByVisitId(Long visitId);
}