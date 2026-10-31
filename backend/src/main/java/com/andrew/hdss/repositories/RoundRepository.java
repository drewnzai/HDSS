package com.andrew.hdss.repositories;

import com.andrew.hdss.models.Round;
import com.andrew.hdss.models.enums.RoundStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoundRepository extends JpaRepository<Round, Long> {

    boolean existsByDatabaseName(String databaseName);

    @EntityGraph(attributePaths = {"createdBy"})
    List<Round> findAllByOrderByCreatedAtDesc();

    boolean existsByStatus(RoundStatus status);
}