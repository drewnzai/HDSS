package com.andrew.hdss.repositories;

import com.andrew.hdss.models.Form;
import com.andrew.hdss.models.enums.FormCategory;
import com.andrew.hdss.models.enums.FormTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormRepository extends JpaRepository<Form, Long> {
    Optional<Form> findByName(String name);
    List<Form> findByCategory(FormCategory category);
    List<Form> findByTarget(FormTarget target);
    List<Form> findByActiveTrue();
}
