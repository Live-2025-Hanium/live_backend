package com.example.live_backend.domain.survey.repository;

import com.example.live_backend.domain.survey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    
    @EntityGraph(attributePaths = {"answers", "answers.surveyQuestion", "answers.selectedOption"})
    List<SurveyResponse> findByMember_IdOrderByCreatedAtDesc(Long memberId);
    
    @EntityGraph(attributePaths = {"answers", "answers.surveyQuestion"})
    @Query("SELECT sr FROM SurveyResponse sr WHERE sr.createdAt BETWEEN :startDate AND :endDate ORDER BY sr.createdAt DESC")
    List<SurveyResponse> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    Long countByMember_Id(Long memberId);

} 