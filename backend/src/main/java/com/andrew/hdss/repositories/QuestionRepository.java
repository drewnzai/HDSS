package com.andrew.hdss.repositories;

import com.andrew.hdss.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByFormIdOrderByOrderIndexAsc(Long formId);

    long countByFormId(Long formId);

    @Query("select coalesce(max(q.orderIndex), -1) from Question q where q.form.id = :formId")
    int findMaxOrderIndexByFormId(@Param("formId") Long formId);
}