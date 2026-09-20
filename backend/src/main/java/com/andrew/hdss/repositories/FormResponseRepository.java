package com.andrew.hdss.repositories;

import com.andrew.hdss.models.FormResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormResponseRepository extends JpaRepository<FormResponse, Long> {
    boolean existsByFormId(Long formId);
}