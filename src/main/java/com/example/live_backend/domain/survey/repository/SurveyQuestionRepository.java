package com.example.live_backend.domain.survey.repository;

import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    @Query("SELECT q FROM SurveyQuestion q LEFT JOIN FETCH q.options WHERE q.isActive = true ORDER BY q.questionNumber ASC")
    List<SurveyQuestion> findActiveQuestionsWithOptions();
    
    @Query("SELECT DISTINCT q FROM SurveyQuestion q " +
           "LEFT JOIN FETCH q.options o " +
           "WHERE q.questionNumber IN :numbers " +
           "AND q.isActive = true " +
           "ORDER BY q.questionNumber ASC")
    List<SurveyQuestion> findByQuestionNumberInWithOptions(@Param("numbers") List<Integer> numbers);
    
    Optional<SurveyQuestion> findByQuestionNumber(Integer questionNumber);
    
    boolean existsByQuestionNumber(Integer questionNumber);
}