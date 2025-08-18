package com.example.live_backend.domain.survey.repository;

import com.example.live_backend.domain.survey.entity.SurveyQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionOptionRepository extends JpaRepository<SurveyQuestionOption, Long> {
    
    List<SurveyQuestionOption> findBySurveyQuestionIdAndIsActiveTrueOrderByOptionNumberAsc(Long questionId);
    
    List<SurveyQuestionOption> findBySurveyQuestionIdOrderByOptionNumberAsc(Long questionId);
}