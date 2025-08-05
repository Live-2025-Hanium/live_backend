package com.example.live_backend.domain.survey.service;

import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.security.SecurityUtil;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.survey.dto.request.SurveySubmissionDto;
import com.example.live_backend.domain.survey.dto.response.SurveySubmissionResponseDto;
import com.example.live_backend.domain.survey.dto.response.SurveyResponseListDto;
import com.example.live_backend.domain.survey.entity.SurveyAnswer;
import com.example.live_backend.domain.survey.entity.SurveyResponse;
import com.example.live_backend.domain.survey.repository.SurveyResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("설문 서비스 테스트")
class SurveyServiceTest {

	@Mock
	private SurveyResponseRepository surveyResponseRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private SecurityUtil securityUtil;

	@InjectMocks
	private SurveyService surveyService;

	private SurveySubmissionDto validRequest;
	private SurveyResponse mockSurveyResponse;
	private Member mockMember;
	private final Long MOCK_USER_ID = 1L;

	@BeforeEach
	void initFixtures() {
		// mockMember 먼저 초기화
		mockMember = org.mockito.Mockito.mock(Member.class);
		given(mockMember.getId()).willReturn(MOCK_USER_ID);
		// void 메서드는 doNothing()으로 설정
		org.mockito.Mockito.doNothing().when(mockMember).updateLastSurveySubmittedAt(any(LocalDateTime.class));

		// 기본 mock 설정
		given(securityUtil.getCurrentUserId()).willReturn(MOCK_USER_ID);
		given(memberRepository.findById(MOCK_USER_ID)).willReturn(Optional.of(mockMember));
		
		List<SurveySubmissionDto.SurveyAnswerDto> answers = Arrays.asList(
			new SurveySubmissionDto.SurveyAnswerDto(1, 1),
			new SurveySubmissionDto.SurveyAnswerDto(2, 2),
			new SurveySubmissionDto.SurveyAnswerDto(3, 3),
			new SurveySubmissionDto.SurveyAnswerDto(4, 4),
			new SurveySubmissionDto.SurveyAnswerDto(5, 5),
			new SurveySubmissionDto.SurveyAnswerDto(6, 1),
			new SurveySubmissionDto.SurveyAnswerDto(7, 2),
			new SurveySubmissionDto.SurveyAnswerDto(8, 3),
			new SurveySubmissionDto.SurveyAnswerDto(9, 4),
			new SurveySubmissionDto.SurveyAnswerDto(10, 5),
			new SurveySubmissionDto.SurveyAnswerDto(11, 1),
			new SurveySubmissionDto.SurveyAnswerDto(12, 2),
			new SurveySubmissionDto.SurveyAnswerDto(13, 3),
			new SurveySubmissionDto.SurveyAnswerDto(14, 4),
			new SurveySubmissionDto.SurveyAnswerDto(15, 5)
		);
		validRequest = new SurveySubmissionDto(answers);

		mockSurveyResponse = SurveyResponse.builder()
			.member(mockMember)
			.build();

		setField(mockSurveyResponse, "id", 100L);
		setField(mockSurveyResponse, "createdAt", LocalDateTime.now());
		answers.forEach(dto -> {
			SurveyAnswer answer = SurveyAnswer.builder()
				.questionNumber(dto.getQuestionNumber())
				.answerNumber(dto.getAnswerNumber())
				.build();
			mockSurveyResponse.addAnswer(answer);
		});
	}

	@Nested
	@DisplayName("설문 제출 기능")
	class SubmissionTests {

		@Test
		@DisplayName("정상적인 설문 제출 - 성공")
		void givenValidRequest_whenSubmitSurvey_thenSuccessfulSubmission() {
			// given
			LocalDateTime expectedTime = LocalDateTime.now();
			SurveyResponse savedResponse = org.mockito.Mockito.mock(SurveyResponse.class);
			given(savedResponse.getId()).willReturn(123L);
			given(savedResponse.getCreatedAt()).willReturn(expectedTime);
			given(savedResponse.getAnswers()).willReturn(validRequest.getAnswers().stream().map(dto -> 
				SurveyAnswer.builder()
					.questionNumber(dto.getQuestionNumber())
					.answerNumber(dto.getAnswerNumber())
					.build()
			).toList());
			
			given(surveyResponseRepository.save(any(SurveyResponse.class))).willReturn(savedResponse);

			// when
			SurveySubmissionResponseDto result = surveyService.submitSurvey(validRequest);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getResponseId()).isEqualTo(123L);
			assertThat(result.getSubmittedAt()).isEqualTo(expectedTime);
			assertThat(result.getTotalAnswers()).isEqualTo(15);

			verify(securityUtil).getCurrentUserId();
			verify(memberRepository).findById(MOCK_USER_ID);
			verify(surveyResponseRepository).save(any(SurveyResponse.class));
			verify(mockMember).updateLastSurveySubmittedAt(expectedTime);
		}

		@Test
		@DisplayName("설문 제출 - 답변 개수 부족 시 예외")
		void submitSurvey_InsufficientAnswers_ThrowsException() {
			// Given
			List<SurveySubmissionDto.SurveyAnswerDto> insufficient = validRequest.getAnswers().subList(0, 14);
			SurveySubmissionDto req = new SurveySubmissionDto(insufficient);

			// When & Then
			Throwable t = catchThrowable(() -> surveyService.submitSurvey(req));

			assertThat(t)
				.isInstanceOf(CustomException.class)
				.hasMessageContaining("설문 문제는 총 15개입니다. 현재 답변 개수: 14");
			assertThat(((CustomException)t).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
		}

		@Test
		@DisplayName("설문 제출 - 문제 번호 중복 시 예외")
		void submitSurvey_DuplicateQuestionNumber_ThrowsException() {
			// Given
			List<SurveySubmissionDto.SurveyAnswerDto> dup = Arrays.asList(
				new SurveySubmissionDto.SurveyAnswerDto(1, 3),
				new SurveySubmissionDto.SurveyAnswerDto(1, 2),
				new SurveySubmissionDto.SurveyAnswerDto(3, 4),
				new SurveySubmissionDto.SurveyAnswerDto(4, 1),
				new SurveySubmissionDto.SurveyAnswerDto(5, 5),
				new SurveySubmissionDto.SurveyAnswerDto(6, 1),
				new SurveySubmissionDto.SurveyAnswerDto(7, 2),
				new SurveySubmissionDto.SurveyAnswerDto(8, 3),
				new SurveySubmissionDto.SurveyAnswerDto(9, 4),
				new SurveySubmissionDto.SurveyAnswerDto(10, 5),
				new SurveySubmissionDto.SurveyAnswerDto(11, 1),
				new SurveySubmissionDto.SurveyAnswerDto(12, 2),
				new SurveySubmissionDto.SurveyAnswerDto(13, 3),
				new SurveySubmissionDto.SurveyAnswerDto(14, 4),
				new SurveySubmissionDto.SurveyAnswerDto(15, 5)
			);
			SurveySubmissionDto req = new SurveySubmissionDto(dup);

			// When & Then
			Throwable t = catchThrowable(() -> surveyService.submitSurvey(req));

			assertThat(t)
				.isInstanceOf(CustomException.class)
				.hasMessageContaining("문제 번호 1가 중복되었습니다");
			assertThat(((CustomException)t).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
		}

		@Test
		@DisplayName("설문 제출 - 문제 번호 범위 초과 시 예외 (16번)")
		void submitSurvey_QuestionNumberOutOfRange_ThrowsException() {
			// Given - 2번 문제를 빼고 16번을 추가하여 15개 답변 유지
			List<SurveySubmissionDto.SurveyAnswerDto> missing = Arrays.asList(
				new SurveySubmissionDto.SurveyAnswerDto(1, 3),
				new SurveySubmissionDto.SurveyAnswerDto(3, 4),
				new SurveySubmissionDto.SurveyAnswerDto(4, 1),
				new SurveySubmissionDto.SurveyAnswerDto(5, 5),
				new SurveySubmissionDto.SurveyAnswerDto(6, 1),
				new SurveySubmissionDto.SurveyAnswerDto(7, 2),
				new SurveySubmissionDto.SurveyAnswerDto(8, 3),
				new SurveySubmissionDto.SurveyAnswerDto(9, 4),
				new SurveySubmissionDto.SurveyAnswerDto(10, 5),
				new SurveySubmissionDto.SurveyAnswerDto(11, 1),
				new SurveySubmissionDto.SurveyAnswerDto(12, 2),
				new SurveySubmissionDto.SurveyAnswerDto(13, 3),
				new SurveySubmissionDto.SurveyAnswerDto(14, 4),
				new SurveySubmissionDto.SurveyAnswerDto(15, 5),
				new SurveySubmissionDto.SurveyAnswerDto(16, 1)
			);
			SurveySubmissionDto req = new SurveySubmissionDto(missing);

			// When & Then
			Throwable t = catchThrowable(() -> surveyService.submitSurvey(req));

			assertThat(t)
				.isInstanceOf(CustomException.class)
				.hasMessageContaining("문제 번호는 1-15 범위여야 합니다. 입력된 값: 16");
			assertThat(((CustomException)t).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
		}

		@Test
		@DisplayName("설문 제출 - 잘못된 문제 번호 범위 시 예외")
		void submitSurvey_InvalidQuestionNumber_ThrowsException() {
			// Given
			List<SurveySubmissionDto.SurveyAnswerDto> outOfRange = Arrays.asList(
				new SurveySubmissionDto.SurveyAnswerDto(1, 3),
				new SurveySubmissionDto.SurveyAnswerDto(2, 2),
				new SurveySubmissionDto.SurveyAnswerDto(3, 4),
				new SurveySubmissionDto.SurveyAnswerDto(4, 1),
				new SurveySubmissionDto.SurveyAnswerDto(5, 5),
				new SurveySubmissionDto.SurveyAnswerDto(6, 1),
				new SurveySubmissionDto.SurveyAnswerDto(7, 2),
				new SurveySubmissionDto.SurveyAnswerDto(8, 3),
				new SurveySubmissionDto.SurveyAnswerDto(9, 4),
				new SurveySubmissionDto.SurveyAnswerDto(10, 5),
				new SurveySubmissionDto.SurveyAnswerDto(11, 1),
				new SurveySubmissionDto.SurveyAnswerDto(12, 2),
				new SurveySubmissionDto.SurveyAnswerDto(13, 3),
				new SurveySubmissionDto.SurveyAnswerDto(14, 4),
				new SurveySubmissionDto.SurveyAnswerDto(16, 5)
			);
			SurveySubmissionDto req = new SurveySubmissionDto(outOfRange);

			// When & Then
			Throwable t = catchThrowable(() -> surveyService.submitSurvey(req));

			assertThat(t)
				.isInstanceOf(CustomException.class)
				.hasMessageContaining("문제 번호는 1-15 범위여야 합니다. 입력된 값: 16");
			assertThat(((CustomException)t).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
		}

		@Test
		@DisplayName("설문 제출 - 잘못된 답변 번호 범위 시 예외")
		void submitSurvey_InvalidAnswerNumber_ThrowsException() {
			// Given
			List<SurveySubmissionDto.SurveyAnswerDto> badAnswer = Arrays.asList(
				new SurveySubmissionDto.SurveyAnswerDto(1, 3),
				new SurveySubmissionDto.SurveyAnswerDto(2, 2),
				new SurveySubmissionDto.SurveyAnswerDto(3, 4),
				new SurveySubmissionDto.SurveyAnswerDto(4, 1),
				new SurveySubmissionDto.SurveyAnswerDto(5, 5),
				new SurveySubmissionDto.SurveyAnswerDto(6, 1),
				new SurveySubmissionDto.SurveyAnswerDto(7, 2),
				new SurveySubmissionDto.SurveyAnswerDto(8, 3),
				new SurveySubmissionDto.SurveyAnswerDto(9, 4),
				new SurveySubmissionDto.SurveyAnswerDto(10, 5),
				new SurveySubmissionDto.SurveyAnswerDto(11, 1),
				new SurveySubmissionDto.SurveyAnswerDto(12, 2),
				new SurveySubmissionDto.SurveyAnswerDto(13, 3),
				new SurveySubmissionDto.SurveyAnswerDto(14, 4),
				new SurveySubmissionDto.SurveyAnswerDto(15, 16)
			);
			SurveySubmissionDto req = new SurveySubmissionDto(badAnswer);

			// When & Then
			Throwable t = catchThrowable(() -> surveyService.submitSurvey(req));

			assertThat(t)
				.isInstanceOf(CustomException.class)
				.hasMessageContaining("답변 번호는 1-15 범위여야 합니다. 입력된 값: 16");
			assertThat(((CustomException)t).getErrorCode())
				.isEqualTo(ErrorCode.INVALID_INPUT);
		}

		@Test
		@DisplayName("Spring Security 인증 테스트 - 사용자 ID 추출")
		void submitSurvey_SecurityContextUserId_Success() {
			// Given
			Long expectedId = 999L;
			given(securityUtil.getCurrentUserId()).willReturn(expectedId);
			given(memberRepository.findById(expectedId)).willReturn(Optional.of(mockMember));
			given(mockMember.getId()).willReturn(expectedId);
			given(surveyResponseRepository.save(any(SurveyResponse.class)))
				.willAnswer(invocation -> {
					SurveyResponse saved = invocation.getArgument(0);
					assertThat(saved.getMember().getId()).isEqualTo(expectedId);
					setField(saved, "id", 200L);
					setField(saved, "createdAt", LocalDateTime.now());
					return saved;
				});

			// When
			SurveySubmissionResponseDto result = surveyService.submitSurvey(validRequest);

			// Then
			assertThat(result.getResponseId()).isEqualTo(200L);
			verify(securityUtil).getCurrentUserId();
			verify(surveyResponseRepository).save(any(SurveyResponse.class));
		}
	}

	@Nested
	@DisplayName("조회 기능")
	class QueryTests {

		@Test
		@DisplayName("사용자별 설문 응답 조회 - 성공")
		void getUserSurveyResponses_Success() {
			// Given
			List<SurveyResponse> mockList = List.of(mockSurveyResponse);
			given(surveyResponseRepository.findByMember_IdOrderByCreatedAtDesc(MOCK_USER_ID))
				.willReturn(mockList);
			given(surveyResponseRepository.countByMember_Id(MOCK_USER_ID))
				.willReturn(1L);

			// When
			SurveyResponseListDto.UserSurveyResponseListDto result =
				surveyService.getUserSurveyResponses(MOCK_USER_ID);

			// Then
			assertThat(result.getUserId()).isEqualTo(MOCK_USER_ID);
			assertThat(result.getTotalResponseCount()).isEqualTo(1L);
			assertThat(result.getResponses()).hasSize(1);
			assertThat(result.getResponses().get(0).getUserId()).isEqualTo(MOCK_USER_ID);
			assertThat(result.getResponses().get(0).getAnswers()).hasSize(15);

			verify(surveyResponseRepository)
				.findByMember_IdOrderByCreatedAtDesc(MOCK_USER_ID);
			verify(surveyResponseRepository)
				.countByMember_Id(MOCK_USER_ID);
		}

		@Test
		@DisplayName("기간별 설문 응답 조회 - 성공")
		void getSurveyResponsesByPeriod_Success() {
			// Given
			List<SurveyResponse> mockList = List.of(mockSurveyResponse);
			given(surveyResponseRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
				.willReturn(mockList);

			// When
			List<SurveyResponseListDto> result = surveyService.getSurveyResponsesByPeriod(
				LocalDateTime.now().toLocalDate().minusDays(1),
				LocalDateTime.now().toLocalDate()
			);

			// Then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).getUserId()).isEqualTo(MOCK_USER_ID);
			assertThat(result.get(0).getAnswers()).hasSize(15);

			verify(surveyResponseRepository)
				.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
		}
	}

	// 리팩토링: Spring ReflectionUtils를 활용해서 private 필드 세팅했습니다.
	private void setField(Object target, String fieldName, Object value) {
		var field = ReflectionUtils.findField(target.getClass(), fieldName);
		if (field == null) {
			throw new RuntimeException("필드를 찾을 수 없습니다: " + fieldName);
		}
		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, target, value);
	}

}
