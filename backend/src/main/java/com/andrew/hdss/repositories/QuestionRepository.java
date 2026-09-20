package com.andrew.hdss.repositories;

import com.andrew.hdss.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByFormIdOrderByOrderIndexAsc(Long formId);
    long countByFormId(Long formId);
}