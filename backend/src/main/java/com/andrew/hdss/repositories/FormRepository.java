package com.andrew.hdss.repositories;

import com.andrew.hdss.models.Form;
import com.andrew.hdss.models.enums.FormStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormRepository extends JpaRepository<Form, Long> {
    Optional<Form> findByName(String name);

    Page<Form> findByStatus(FormStatus status, Pageable pageable);
}
