package com.example.live_backend.domain.survey.repository;

import com.example.live_backend.domain.survey.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

} 