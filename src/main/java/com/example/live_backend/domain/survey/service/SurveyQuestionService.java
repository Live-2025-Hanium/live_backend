package com.example.live_backend.domain.survey.service;

import com.example.live_backend.domain.survey.dto.request.CreateQuestionRequest;
import com.example.live_backend.domain.survey.dto.request.UpdateQuestionRequest;
import com.example.live_backend.domain.survey.dto.response.SurveyQuestionDto;
import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import com.example.live_backend.domain.survey.entity.SurveyQuestionOption;
import com.example.live_backend.domain.survey.repository.SurveyQuestionRepository;
import com.example.live_backend.domain.survey.repository.SurveyQuestionOptionRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SurveyQuestionService {
    
    private final SurveyQuestionRepository questionRepository;
    private final SurveyQuestionOptionRepository optionRepository;
    
    /**
     * 모든 활성 질문 조회
     */
    public List<SurveyQuestionDto> getAllActiveQuestions() {
        log.info("활성 설문 질문 목록 조회");
        List<SurveyQuestion> questions = questionRepository.findActiveQuestionsWithOptions();
        return questions.stream()
                .map(SurveyQuestionDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 새로운 질문 생성
     */
    @Transactional
    public SurveyQuestionDto createQuestion(CreateQuestionRequest request) {
        log.info("새 질문 생성 - 질문 번호: {}", request.getQuestionNumber());
        
        // 질문 번호 중복 체크
        if (questionRepository.existsByQuestionNumber(request.getQuestionNumber())) {
            throw new CustomException(ErrorCode.INVALID_INPUT, 
                    "이미 존재하는 질문 번호입니다: " + request.getQuestionNumber());
        }
        
        SurveyQuestion question = SurveyQuestion.builder()
                .questionNumber(request.getQuestionNumber())
                .questionText(request.getQuestionText())
                .isRequired(request.isRequired())
                .isActive(request.isActive())
                .build();
        
        // 옵션이 있는 경우 추가
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (CreateQuestionRequest.CreateOptionRequest optionRequest : request.getOptions()) {
                SurveyQuestionOption option = SurveyQuestionOption.builder()
                        .surveyQuestion(question)
                        .optionNumber(optionRequest.getOptionNumber())
                        .optionText(optionRequest.getOptionText())
                        .isActive(optionRequest.isActive())
                        .build();
                question.addOption(option);
            }
        }
        
        SurveyQuestion saved = questionRepository.save(question);
        log.info("질문 생성 완료 - ID: {}", saved.getId());
        
        return SurveyQuestionDto.from(saved);
    }
    
    /**
     * 질문 수정
     */
    @Transactional
    public SurveyQuestionDto updateQuestion(Long questionId, UpdateQuestionRequest request) {
        log.info("질문 수정 - ID: {}", questionId);
        
        SurveyQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SURVEY_NOT_FOUND, "질문을 찾을 수 없습니다"));
        
        question.updateQuestion(
                request.getQuestionText(),
                request.isRequired(),
                request.isActive()
        );
        
        SurveyQuestion updated = questionRepository.save(question);
        log.info("질문 수정 완료 - ID: {}", updated.getId());
        
        return SurveyQuestionDto.from(updated);
    }
    
    /**
     * 질문 삭제 (비활성화)
     */
    @Transactional
    public void deactivateQuestion(Long questionId) {
        log.info("질문 비활성화 - ID: {}", questionId);
        
        SurveyQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SURVEY_NOT_FOUND, "질문을 찾을 수 없습니다"));
        
        question.updateQuestion(
                question.getQuestionText(),
                question.isRequired(),
                false // 비활성화
        );
        
        questionRepository.save(question);
        log.info("질문 비활성화 완료 - ID: {}", questionId);
    }
}