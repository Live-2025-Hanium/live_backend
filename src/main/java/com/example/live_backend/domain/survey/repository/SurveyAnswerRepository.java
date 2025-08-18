package com.example.live_backend.domain.survey.repository;

import com.example.live_backend.domain.survey.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {
    
    /**
     * 특정 설문 응답의 모든 답변 조회
     */
    @Query("SELECT sa FROM SurveyAnswer sa JOIN FETCH sa.surveyQuestion WHERE sa.surveyResponse.id = :surveyResponseId ORDER BY sa.surveyQuestion.questionNumber")
    List<SurveyAnswer> findBySurveyResponseIdWithQuestion(@Param("surveyResponseId") Long surveyResponseId);
    
    /**
     * 특정 질문에 대한 답변별 통계 (관리자용)
     */
    @Query("SELECT sa.selectedOption.optionNumber, COUNT(sa) FROM SurveyAnswer sa WHERE sa.surveyQuestion.questionNumber = :questionNumber AND sa.selectedOption IS NOT NULL GROUP BY sa.selectedOption.optionNumber ORDER BY sa.selectedOption.optionNumber")
    List<Object[]> getAnswerStatsByQuestionNumber(@Param("questionNumber") Integer questionNumber);
    
    /**
     * 특정 응답과 질문에 대한 답변 조회
     */
    Optional<SurveyAnswer> findBySurveyResponseIdAndSurveyQuestionId(Long surveyResponseId, Long questionId);
} 