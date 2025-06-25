package com.example.live_backend.domain.survey.service;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.domain.memeber.util.UserUtil;
import com.example.live_backend.domain.survey.dto.request.SurveySubmissionDto;
import com.example.live_backend.domain.survey.dto.response.SurveySubmissionResponseDto;
import com.example.live_backend.domain.survey.dto.response.SurveyResponseListDto;
import com.example.live_backend.domain.survey.entity.SurveyAnswer;
import com.example.live_backend.domain.survey.entity.SurveyResponse;
import com.example.live_backend.domain.survey.repository.SurveyResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyService {

    private final SurveyResponseRepository surveyResponseRepository;
    private final UserUtil userUtil;
    
    private static final int TOTAL_QUESTIONS = 5;
    private static final int MIN_ANSWER_NUMBER = 1;
    private static final int MAX_ANSWER_NUMBER = 5;

    /**
     * 설문 응답 제출
     */
    @Transactional
    public SurveySubmissionResponseDto submitSurvey(SurveySubmissionDto request) {

        Long currentUserId = userUtil.getCurrentUserId();
        log.info("설문 응답 제출 시작 - 인증된 사용자 ID: {}", currentUserId);

        validateSurveyAnswers(request.getAnswers());

        SurveyResponse surveyResponse = SurveyResponse.builder()
                .userId(currentUserId)
                .build();

        for (SurveySubmissionDto.SurveyAnswerDto answerDto : request.getAnswers()) {
            SurveyAnswer answer = SurveyAnswer.builder()
                    .questionNumber(answerDto.getQuestionNumber())
                    .answerNumber(answerDto.getAnswerNumber())
                    .build();
            
            surveyResponse.addAnswer(answer);
        }

        SurveyResponse savedResponse = surveyResponseRepository.save(surveyResponse);
        
        log.info("설문 응답 제출 완료 - 응답 ID: {}, 사용자 ID: {}", savedResponse.getId(), currentUserId);
        
        return SurveySubmissionResponseDto.builder()
                .responseId(savedResponse.getId())
                .submittedAt(savedResponse.getCreatedAt())
                .totalAnswers(savedResponse.getAnswers().size())
                .build();
    }

     /**
      * 설문 답변 데이터 유효성 검증
      */
     private void validateSurveyAnswers(List<SurveySubmissionDto.SurveyAnswerDto> answers) {
         // 답변 개수 검증
         if (answers.size() != TOTAL_QUESTIONS) {
             throw new CustomException(ErrorCode.INVALID_INPUT, 
                     String.format("설문 문제는 총 %d개입니다. 현재 답변 개수: %d", TOTAL_QUESTIONS, answers.size()));
         }
         
         // 문제 번호 중복 및 범위 검증
         Set<Integer> questionNumbers = new HashSet<>();
         
         for (SurveySubmissionDto.SurveyAnswerDto answer : answers) {
             // 문제 번호 범위 검증
             if (answer.getQuestionNumber() < 1 || answer.getQuestionNumber() > TOTAL_QUESTIONS) {
                 throw new CustomException(ErrorCode.INVALID_INPUT, 
                         String.format("문제 번호는 1-%d 범위여야 합니다. 입력된 값: %d", TOTAL_QUESTIONS, answer.getQuestionNumber()));
             }
             
             // 답변 번호 범위 검증
             if (answer.getAnswerNumber() < MIN_ANSWER_NUMBER || answer.getAnswerNumber() > MAX_ANSWER_NUMBER) {
                 throw new CustomException(ErrorCode.INVALID_INPUT, 
                         String.format("답변 번호는 %d-%d 범위여야 합니다. 입력된 값: %d", MIN_ANSWER_NUMBER, MAX_ANSWER_NUMBER, answer.getAnswerNumber()));
             }
             
             // 문제 번호 중복 검증
             if (!questionNumbers.add(answer.getQuestionNumber())) {
                 throw new CustomException(ErrorCode.INVALID_INPUT, 
                         String.format("문제 번호 %d가 중복되었습니다.", answer.getQuestionNumber()));
             }
         }
         
         // 모든 문제에 답변했는지 검증
         for (int i = 1; i <= TOTAL_QUESTIONS; i++) {
             if (!questionNumbers.contains(i)) {
                 throw new CustomException(ErrorCode.INVALID_INPUT, 
                         String.format("문제 %d번에 대한 답변이 누락되었습니다.", i));
             }
         }
     }
     

     
     /**
      * 특정 사용자의 설문 응답 목록 조회 (관리자용)
      */
     @Transactional(readOnly = true)
     public List<SurveyResponseListDto> getUserSurveyResponses(Long userId) {
         log.info("사용자 설문 응답 조회 - 사용자 ID: {}", userId);
         
         List<SurveyResponse> responses = surveyResponseRepository.findByUserIdOrderByCreatedAtDesc(userId);
         
         return responses.stream()
                 .map(this::convertToResponseListDto)
                 .toList();
     }
     
     /**
      * 특정 기간 내 모든 설문 응답 조회 (관리자용)
      */
     @Transactional(readOnly = true)
     public List<SurveyResponseListDto> getSurveyResponsesByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
         log.info("기간별 설문 응답 조회 - 시작: {}, 종료: {}", startDate, endDate);
         
         List<SurveyResponse> responses = surveyResponseRepository.findByCreatedAtBetween(startDate, endDate);
         
         return responses.stream()
                 .map(this::convertToResponseListDto)
             .toList();
     }
     
     /**
      * 사용자의 설문 응답 횟수 조회
      */
     @Transactional(readOnly = true)
     public Long getUserSurveyCount(Long userId) {
         return surveyResponseRepository.countByUserId(userId);
     }
     
     private SurveyResponseListDto convertToResponseListDto(SurveyResponse response) {
         List<SurveyResponseListDto.SurveyAnswerDetailDto> answerDtos = response.getAnswers().stream()
                 .map(answer -> SurveyResponseListDto.SurveyAnswerDetailDto.builder()
                         .questionNumber(answer.getQuestionNumber())
                         .answerNumber(answer.getAnswerNumber())
                         .build())
                 .toList();
         
         return SurveyResponseListDto.builder()
                 .responseId(response.getId())
                 .userId(response.getUserId())
                 .submittedAt(response.getCreatedAt())
                 .answers(answerDtos)
                 .build();
     }
} 