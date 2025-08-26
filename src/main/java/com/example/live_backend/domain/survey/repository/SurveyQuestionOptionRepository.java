package com.example.live_backend.domain.survey.repository;

import com.example.live_backend.domain.survey.entity.SurveyQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyQuestionOptionRepository extends JpaRepository<SurveyQuestionOption, Long> {
}