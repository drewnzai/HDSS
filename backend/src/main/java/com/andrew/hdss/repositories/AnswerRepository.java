package com.andrew.hdss.repositories;

import com.andrew.hdss.models.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByFormResponseId(Long formResponseId);
    Optional<Answer> findByFormResponseIdAndQuestionId(Long formResponseId, Long questionId);
    Optional<Answer> findByClientId(String clientId);
}
