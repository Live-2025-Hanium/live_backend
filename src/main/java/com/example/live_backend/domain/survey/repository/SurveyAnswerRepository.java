package com.example.live_backend.domain.survey.repository;

import com.example.live_backend.domain.survey.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {
    
    /**
     * 특정 설문 응답의 모든 답변 조회
     */
    List<SurveyAnswer> findBySurveyResponseIdOrderByQuestionNumber(Long surveyResponseId);
    
    /**
     * 특정 문제에 대한 답변별 통계 (관리자용)
     */
    @Query("SELECT sa.answerNumber, COUNT(sa) FROM SurveyAnswer sa WHERE sa.questionNumber = :questionNumber GROUP BY sa.answerNumber ORDER BY sa.answerNumber")
    List<Object[]> getAnswerStatsByQuestionNumber(@Param("questionNumber") Integer questionNumber);
} 