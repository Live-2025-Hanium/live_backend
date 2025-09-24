package com.example.live_backend.domain.survey.service;

import com.example.live_backend.domain.survey.dto.request.CreateQuestionRequest;
import com.example.live_backend.domain.survey.dto.request.UpdateQuestionRequest;
import com.example.live_backend.domain.survey.dto.response.SurveyQuestionDto;
import com.example.live_backend.domain.survey.dto.response.SurveyPageResponse;
import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import com.example.live_backend.domain.survey.entity.SurveyQuestionOption;
import com.example.live_backend.domain.survey.repository.SurveyQuestionRepository;
import com.example.live_backend.domain.survey.repository.SurveyQuestionOptionRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SurveyQuestionService {

    private static final int QUESTIONS_PER_PAGE = 5;

    private final SurveyQuestionRepository questionRepository;
    private final SurveyQuestionOptionRepository optionRepository;
    
    @Cacheable(value = "activeQuestions", unless = "#result.isEmpty()")
    public List<SurveyQuestionDto> getAllActiveQuestions() {
        log.info("활성 설문 질문 목록 조회");
        List<SurveyQuestion> questions = questionRepository.findActiveQuestionsWithOptions();
        return questions.stream()
                .map(SurveyQuestionDto::from)
                .collect(Collectors.toList());
    }

    public SurveyPageResponse getQuestionsByPage(int pageNumber) {
        log.info("설문 질문 페이지별 조회 - 페이지: {}", pageNumber);

        if (pageNumber < 1) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "페이지 번호는 1 이상이어야 합니다");
        }

        List<SurveyQuestionDto> allQuestions = getAllActiveQuestions();

        int totalQuestions = allQuestions.size();
        int totalPages = (int) Math.ceil((double) totalQuestions / QUESTIONS_PER_PAGE);

        if (pageNumber > totalPages && totalPages > 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT,
                    String.format("페이지 번호가 범위를 초과했습니다. 최대 페이지: %d", totalPages));
        }

        if (totalQuestions == 0) {
            return SurveyPageResponse.of(
                    List.of(),
                    1,
                    0,
                    0,
                    QUESTIONS_PER_PAGE
            );
        }

        int startIndex = (pageNumber - 1) * QUESTIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + QUESTIONS_PER_PAGE, totalQuestions);

        List<SurveyQuestionDto> pageQuestions = allQuestions.subList(startIndex, endIndex);

        log.info("페이지 {} - 질문 {}번부터 {}번까지 반환",
                pageNumber,
                pageQuestions.get(0).getQuestionNumber(),
                pageQuestions.get(pageQuestions.size() - 1).getQuestionNumber());

        return SurveyPageResponse.of(
                pageQuestions,
                pageNumber,
                totalPages,
                totalQuestions,
                QUESTIONS_PER_PAGE
        );
    }
    
    @CacheEvict(value = "activeQuestions", allEntries = true)
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
                .questionType(request.getQuestionType())
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
    
    @CacheEvict(value = "activeQuestions", allEntries = true)
    @Transactional
    public SurveyQuestionDto updateQuestion(Long questionId, UpdateQuestionRequest request) {
        log.info("질문 수정 - ID: {}", questionId);
        
        SurveyQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SURVEY_NOT_FOUND, "질문을 찾을 수 없습니다"));
        
        question.updateQuestion(
                request.getQuestionText(),
                question.getQuestionType(), // 기존 타입 유지
                request.isRequired(),
                request.isActive()
        );
        
        SurveyQuestion updated = questionRepository.save(question);
        log.info("질문 수정 완료 - ID: {}", updated.getId());
        
        return SurveyQuestionDto.from(updated);
    }
    
    @CacheEvict(value = "activeQuestions", allEntries = true)
    @Transactional
    public void deactivateQuestion(Long questionId) {
        log.info("질문 비활성화 - ID: {}", questionId);
        
        SurveyQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SURVEY_NOT_FOUND, "질문을 찾을 수 없습니다"));
        
        question.updateQuestion(
                question.getQuestionText(),
                question.getQuestionType(), // 기존 타입 유지
                question.isRequired(),
                false // 비활성화
        );
        
        questionRepository.save(question);
        log.info("질문 비활성화 완료 - ID: {}", questionId);
    }
}