package com.example.live_backend.domain.survey.repository;

import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
    
    List<SurveyQuestion> findByIsActiveTrueOrderByQuestionNumberAsc();
    
    @Query("SELECT q FROM SurveyQuestion q LEFT JOIN FETCH q.options WHERE q.isActive = true ORDER BY q.questionNumber ASC")
    List<SurveyQuestion> findActiveQuestionsWithOptions();
    
    Optional<SurveyQuestion> findByQuestionNumber(Integer questionNumber);
    
    boolean existsByQuestionNumber(Integer questionNumber);
}