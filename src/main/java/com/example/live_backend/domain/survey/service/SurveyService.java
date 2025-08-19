package com.example.live_backend.domain.survey.service;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.survey.dto.request.SurveySubmissionDto;
import com.example.live_backend.domain.survey.dto.response.SurveySubmissionResponseDto;
import com.example.live_backend.domain.survey.dto.response.SurveyResponseListDto;
import com.example.live_backend.domain.survey.entity.SurveyAnswer;
import com.example.live_backend.domain.survey.entity.SurveyResponse;
import com.example.live_backend.domain.survey.entity.SurveyQuestion;
import com.example.live_backend.domain.survey.entity.SurveyQuestionOption;
import com.example.live_backend.domain.survey.repository.SurveyResponseRepository;
import com.example.live_backend.domain.survey.repository.SurveyQuestionRepository;
import com.example.live_backend.domain.survey.repository.SurveyQuestionOptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyService {

    private final SurveyResponseRepository surveyResponseRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    private final MemberRepository memberRepository;

    private static final int TOTAL_QUESTIONS = 15;
    private static final int MIN_ANSWER_NUMBER = 1;
    private static final int MAX_ANSWER_NUMBER = 15;


    @Transactional
    public SurveySubmissionResponseDto submitSurvey(SurveySubmissionDto request, Long memberId) {
        log.info("설문 응답 제출 시작 - 인증된 사용자 ID: {}", memberId);

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<SurveySubmissionDto.SurveyAnswerDto> answers = request.getAnswers();
        validateAnswerCount(answers);
        validateQuestionNumberRange(answers);
        validateAnswerNumberRange(answers);
        validateDuplicateQuestions(answers);
        validateMissingQuestions(answers);

        List<Integer> questionNumbers = answers.stream()
            .map(SurveySubmissionDto.SurveyAnswerDto::getQuestionNumber)
            .collect(Collectors.toList());
        
        Map<Integer, SurveyQuestion> questionMap = surveyQuestionRepository
            .findByQuestionNumberInWithOptions(questionNumbers)
            .stream()
            .collect(Collectors.toMap(SurveyQuestion::getQuestionNumber, q -> q));

        if (questionMap.size() != answers.size()) {
            throw new CustomException(ErrorCode.SURVEY_NOT_FOUND, 
                "일부 질문을 찾을 수 없습니다. 요청: " + answers.size() + ", 조회: " + questionMap.size());
        }

        SurveyResponse surveyResponse = SurveyResponse.builder()
            .member(member)
            .build();

        for (var dto : answers) {
            SurveyQuestion question = questionMap.get(dto.getQuestionNumber());
            
            SurveyQuestionOption selectedOption = question.getOptions().stream()
                .filter(opt -> opt.getOptionNumber().equals(dto.getAnswerNumber()))
                .findFirst()
                .orElse(null);
            
            SurveyAnswer answer = SurveyAnswer.builder()
                .surveyQuestion(question)
                .selectedOption(selectedOption)
                .numberAnswer(dto.getAnswerNumber())
                .build();
            surveyResponse.addAnswer(answer);
        }

        SurveyResponse saved = surveyResponseRepository.save(surveyResponse);

        member.updateLastSurveySubmittedAt(saved.getCreatedAt());
        
        log.info("설문 응답 제출 완료 - 응답 ID: {}, 사용자 ID: {}", saved.getId(), memberId);

        return SurveySubmissionResponseDto.builder()
            .responseId(saved.getId())
            .submittedAt(saved.getCreatedAt())
            .totalAnswers(saved.getAnswers().size())
            .build();
    }

    private void validateAnswerCount(List<SurveySubmissionDto.SurveyAnswerDto> answers) {
        if (answers.size() != TOTAL_QUESTIONS) {
            throw new CustomException(
                ErrorCode.INVALID_INPUT,
                String.format("설문 문제는 총 %d개입니다. 현재 답변 개수: %d", TOTAL_QUESTIONS, answers.size())
            );
        }
    }

    private void validateQuestionNumberRange(List<SurveySubmissionDto.SurveyAnswerDto> answers) {
        for (var dto : answers) {
            int q = dto.getQuestionNumber();
            if (q < 1 || q > TOTAL_QUESTIONS) {
                throw new CustomException(
                    ErrorCode.INVALID_INPUT,
                    String.format("문제 번호는 1-%d 범위여야 합니다. 입력된 값: %d", TOTAL_QUESTIONS, q)
                );
            }
        }
    }

    private void validateAnswerNumberRange(List<SurveySubmissionDto.SurveyAnswerDto> answers) {
        for (var dto : answers) {
            int a = dto.getAnswerNumber();
            if (a < MIN_ANSWER_NUMBER || a > MAX_ANSWER_NUMBER) {
                throw new CustomException(
                    ErrorCode.INVALID_INPUT,
                    String.format("답변 번호는 %d-%d 범위여야 합니다. 입력된 값: %d",
                        MIN_ANSWER_NUMBER, MAX_ANSWER_NUMBER, a)
                );
            }
        }
    }

    private void validateDuplicateQuestions(List<SurveySubmissionDto.SurveyAnswerDto> answers) {
        Set<Integer> seen = new HashSet<>();
        for (var dto : answers) {
            if (!seen.add(dto.getQuestionNumber())) {
                throw new CustomException(
                    ErrorCode.INVALID_INPUT,
                    String.format("문제 번호 %d가 중복되었습니다.", dto.getQuestionNumber())
                );
            }
        }
    }

    private void validateMissingQuestions(List<SurveySubmissionDto.SurveyAnswerDto> answers) {
        Set<Integer> seen = answers.stream()
            .map(SurveySubmissionDto.SurveyAnswerDto::getQuestionNumber)
            .collect(Collectors.toSet());
        for (int i = 1; i <= TOTAL_QUESTIONS; i++) {
            if (!seen.contains(i)) {
                throw new CustomException(
                    ErrorCode.INVALID_INPUT,
                    String.format("문제 %d번에 대한 답변이 누락되었습니다.", i)
                );
            }
        }
    }
     
     /**
      * 특정 회원의 설문 응답 목록 조회 (관리자용)
      */
     @Transactional(readOnly = true)
     public SurveyResponseListDto.UserSurveyResponseListDto getUserSurveyResponses(Long memberId) {
         log.info("회원 설문 응답 조회 - 회원 ID: {}", memberId);
         
                 List<SurveyResponse> responses = surveyResponseRepository.findByMember_IdOrderByCreatedAtDesc(memberId);
        Long totalCount = surveyResponseRepository.countByMember_Id(memberId);
         
         List<SurveyResponseListDto> responseDtos = responses.stream()
                 .map(this::convertToResponseListDto)
                 .toList();
         
         return SurveyResponseListDto.UserSurveyResponseListDto.builder()
                 .userId(memberId)
                 .totalResponseCount(totalCount)
                 .responses(responseDtos)
                 .build();
     }
     
     /**
      * 특정 기간 내 모든 설문 응답 조회 (관리자용)
      */
     @Transactional(readOnly = true)
     public List<SurveyResponseListDto> getSurveyResponsesByPeriod(LocalDate startDate, LocalDate endDate) {
         log.info("기간별 설문 응답 조회 - 시작: {}, 종료: {}", startDate, endDate);
         
         LocalDateTime startDateTime = startDate.atStartOfDay();
         LocalDateTime endDateTime = endDate.atTime(23, 59, 59, 999999999);
         
         List<SurveyResponse> responses = surveyResponseRepository.findByCreatedAtBetween(startDateTime, endDateTime);
         
         return responses.stream()
                 .map(this::convertToResponseListDto)
             .toList();
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
                 .userId(response.getMemberId())
                 .submittedAt(response.getCreatedAt())
                 .answers(answerDtos)
                 .build();
     }
} 