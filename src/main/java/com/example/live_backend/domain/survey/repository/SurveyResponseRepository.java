package com.example.live_backend.domain.survey.repository;

import com.example.live_backend.domain.survey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    
    /**
     * 특정 회원의 설문 응답 목록을 최신순으로 조회
     */
    List<SurveyResponse> findByMember_IdOrderByCreatedAtDesc(Long memberId);
    
    /**
     * 특정 기간 내 설문 응답 조회 (관리자용)
     */
    @Query("SELECT sr FROM SurveyResponse sr WHERE sr.createdAt BETWEEN :startDate AND :endDate ORDER BY sr.createdAt DESC")
    List<SurveyResponse> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * 회원별 설문 응답 횟수 조회
     */
    Long countByMember_Id(Long memberId);

} 