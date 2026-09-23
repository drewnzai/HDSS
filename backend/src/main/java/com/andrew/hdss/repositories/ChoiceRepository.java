package com.andrew.hdss.repositories;

import com.andrew.hdss.models.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChoiceRepository extends JpaRepository<Choice, Long> {
    List<Choice> findByListName(String listName);
    boolean existsByListNameAndName(String listName, String name);
    boolean existsByListNameAndNameAndIdNot(String listName, String name, Long id);
}
