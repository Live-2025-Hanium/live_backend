package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.memeber.Gender;
import com.example.live_backend.domain.memeber.Role;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.mission.clover.Enum.CloverMissionStatus;
import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordRequestDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionRecordResponseDto;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRecordRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("클로버 미션 기록 서비스 테스트")
class CloverMissionRecordServiceTest {

	@InjectMocks
	private CloverMissionRecordService cloverMissionRecordService;

	@Mock
	private CloverMissionRecordRepository missionRecordRepository;

	private Member mockMember;
	private final Long TEST_MEMBER_ID = 1L;
	private final Long TEST_USER_MISSION_ID = 10L;

	@BeforeEach
	void setUp() {
		mockMember = Member.builder()
				.email("mockuser@example.com")
				.oauthId("oauth-1")
				.role(Role.USER)
				.profile(new Profile("Mock", "https://example.com/p.png"))
				.gender(Gender.FEMALE)
				.build();
		ReflectionTestUtils.setField(mockMember, "id", TEST_MEMBER_ID);
	}

	private CloverMissionRecord createCompletedRecord(CloverType type) {
		CloverMissionRecord record = CloverMissionRecord.builder()
				.id(TEST_USER_MISSION_ID)
				.member(mockMember)
				.missionId(100L)
				.missionTitle("테스트 미션")
				.cloverMissionStatus(CloverMissionStatus.COMPLETED)
				.cloverType(type)
				.missionDifficulty(MissionDifficulty.NORMAL)
				.build();
		return record;
	}

	private CloverMissionRecord createStartedRecord(CloverType type) {
		CloverMissionRecord record = CloverMissionRecord.builder()
				.id(TEST_USER_MISSION_ID)
				.member(mockMember)
				.missionId(100L)
				.missionTitle("테스트 미션")
				.cloverMissionStatus(CloverMissionStatus.STARTED)
				.cloverType(type)
				.missionDifficulty(MissionDifficulty.NORMAL)
				.build();
		return record;
	}

	private CloverMissionRecordRequestDto buildRequest(Long userMissionId, String comment, MissionDifficulty difficulty, String imageUrl) {
		CloverMissionRecordRequestDto dto = new CloverMissionRecordRequestDto();
		ReflectionTestUtils.setField(dto, "userMissionId", userMissionId);
		ReflectionTestUtils.setField(dto, "feedbackComment", comment);
		ReflectionTestUtils.setField(dto, "feedbackDifficulty", difficulty);
		ReflectionTestUtils.setField(dto, "imageUrl", imageUrl);
		return dto;
	}

	@Nested
	@DisplayName("미션 기록 피드백 추가")
	class AddMissionRecordTests {
		@Test
		@DisplayName("성공 - 비포토 타입 피드백 추가")
		void addMissionRecord_success_nonPhoto() {

			// Given
			CloverMissionRecord record = createCompletedRecord(CloverType.TIMER);
			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.of(record));
			CloverMissionRecordRequestDto request = buildRequest(TEST_USER_MISSION_ID, "comment", MissionDifficulty.EASY, null);

			// When
			CloverMissionRecordResponseDto response = cloverMissionRecordService.addMissionRecord(TEST_MEMBER_ID, request);

			// Then
			assertThat(response.getUserMissionId()).isEqualTo(TEST_USER_MISSION_ID);
			assertThat(response.getFeedbackComment()).isEqualTo("comment");
			assertThat(response.getFeedbackDifficulty()).isEqualTo(MissionDifficulty.EASY);
		}

		@Test
		@DisplayName("성공 - 포토 타입 피드백(이미지 포함) 추가")
		void addMissionRecord_success_photo() {

			// Given
			CloverMissionRecord record = createCompletedRecord(CloverType.PHOTO);
			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.of(record));
			CloverMissionRecordRequestDto request = buildRequest(TEST_USER_MISSION_ID, "사진 업로드", MissionDifficulty.NORMAL, "https://s3/url.jpg");

			// When
			CloverMissionRecordResponseDto response = cloverMissionRecordService.addMissionRecord(TEST_MEMBER_ID, request);

			// Then
			assertThat(response.getUserMissionId()).isEqualTo(TEST_USER_MISSION_ID);
			assertThat(response.getImageUrl()).isEqualTo("https://s3/url.jpg");
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 미션 기록")
		void addMissionRecord_notFound() {

			// Given
			given(missionRecordRepository.findByIdWithMember(anyLong())).willReturn(Optional.empty());
			CloverMissionRecordRequestDto request = buildRequest(999L, "", MissionDifficulty.EASY, null);

			// When & Then
			CustomException exception = assertThrows(CustomException.class, () -> cloverMissionRecordService.addMissionRecord(TEST_MEMBER_ID, request));
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
		}

		@Test
		@DisplayName("실패 - 다른 사용자의 미션 기록")
		void addMissionRecord_forbidden() {

			// Given
			Member other = Member.builder().email("other@example.com").oauthId("oauth-2").role(Role.USER).profile(new Profile("Other","u")).gender(Gender.MALE).build();
			ReflectionTestUtils.setField(other, "id", 99L);
			CloverMissionRecord record = CloverMissionRecord.builder()
					.id(TEST_USER_MISSION_ID)
					.member(other)
					.missionId(100L)
					.missionTitle("테스트 미션")
					.cloverMissionStatus(CloverMissionStatus.COMPLETED)
					.cloverType(CloverType.TIMER)
					.build();
			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.of(record));
			CloverMissionRecordRequestDto request = buildRequest(TEST_USER_MISSION_ID, "코멘트", MissionDifficulty.EASY, null);

			// When & Then
			CustomException exception = assertThrows(CustomException.class, () -> cloverMissionRecordService.addMissionRecord(TEST_MEMBER_ID, request));
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_FORBIDDEN);
		}

		@Test
		@DisplayName("실패 - 포토 타입 이미지 URL 누락")
		void addMissionRecord_photoMissingImage() {

			// Given
			CloverMissionRecord record = createCompletedRecord(CloverType.PHOTO);
			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.of(record));
			CloverMissionRecordRequestDto request = buildRequest(TEST_USER_MISSION_ID, "comment", MissionDifficulty.EASY, " ");

			// When & Then
			CustomException exception = assertThrows(CustomException.class, () -> cloverMissionRecordService.addMissionRecord(TEST_MEMBER_ID, request));
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.IMAGE_URL_REQUIRED);
		}

		@Test
		@DisplayName("실패 - 완료 상태가 아닌 미션 피드백 추가")
		void addMissionRecord_invalidStatus() {
			// Given
			CloverMissionRecord record = createStartedRecord(CloverType.TIMER);
			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.of(record));
			CloverMissionRecordRequestDto request = buildRequest(TEST_USER_MISSION_ID, "comment", MissionDifficulty.EASY, null);

			// When & Then
			CustomException exception = assertThrows(CustomException.class, () -> cloverMissionRecordService.addMissionRecord(TEST_MEMBER_ID, request));
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MISSION_STATUS);
		}
	}

	@Nested
	@DisplayName("미션 기록 단건 조회")
	class GetMissionRecordTests {
		@Test
		@DisplayName("성공 - 단건 조회")
		void getMissionRecord_success() {

			// Given
			CloverMissionRecord record = createCompletedRecord(CloverType.DISTANCE);
			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.of(record));

			// When
			CloverMissionRecordResponseDto dto = cloverMissionRecordService.getMissionRecord(TEST_MEMBER_ID, TEST_USER_MISSION_ID);

			// Then
			assertThat(dto.getUserMissionId()).isEqualTo(TEST_USER_MISSION_ID);
			verify(missionRecordRepository).findByIdWithMember(TEST_USER_MISSION_ID);
		}

		@Test
		@DisplayName("실패 - 존재하지 않음")
		void getMissionRecord_notFound() {

			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.empty());
			CustomException exception = assertThrows(CustomException.class, () -> cloverMissionRecordService.getMissionRecord(TEST_MEMBER_ID, TEST_USER_MISSION_ID));
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
		}

		@Test
		@DisplayName("실패 - 다른 사용자의 미션 기록")
		void getMissionRecord_forbidden() {
			Member other = Member.builder().email("other@example.com").oauthId("oauth-2").role(Role.USER).profile(new Profile("Other","u")).gender(Gender.MALE).build();
			ReflectionTestUtils.setField(other, "id", 99L);
			CloverMissionRecord record = CloverMissionRecord.builder()
					.id(TEST_USER_MISSION_ID)
					.member(other)
					.missionId(100L)
					.missionTitle("테스트 미션")
					.cloverMissionStatus(CloverMissionStatus.COMPLETED)
					.cloverType(CloverType.TIMER)
					.build();
			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.of(record));

			CustomException exception = assertThrows(CustomException.class, () -> cloverMissionRecordService.getMissionRecord(TEST_MEMBER_ID, TEST_USER_MISSION_ID));
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_FORBIDDEN);
		}
	}

	@Nested
	@DisplayName("미션 기록 수정")
	class UpdateMissionRecordTests {
		@Test
		@DisplayName("성공 - 비포토 타입 수정")
		void updateMissionRecord_success_nonPhoto() {

			// Given
			CloverMissionRecord record = createCompletedRecord(CloverType.TIMER);
			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.of(record));
			CloverMissionRecordRequestDto request = buildRequest(TEST_USER_MISSION_ID, "update comment", MissionDifficulty.HARD, null);

			// When
			CloverMissionRecordResponseDto dto = cloverMissionRecordService.updateMissionRecord(TEST_MEMBER_ID, request);

			// Then
			assertThat(dto.getFeedbackComment()).isEqualTo("update comment");
			assertThat(dto.getFeedbackDifficulty()).isEqualTo(MissionDifficulty.HARD);
		}

		@Test
		@DisplayName("성공 - 포토 타입 이미지 포함 수정")
		void updateMissionRecord_success_photoWithImage() {

			// Given
			CloverMissionRecord record = createCompletedRecord(CloverType.PHOTO);
			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.of(record));
			CloverMissionRecordRequestDto request = buildRequest(TEST_USER_MISSION_ID, "사진 수정", MissionDifficulty.NORMAL, "https://s3/new_image_url.jpg");

			// When
			CloverMissionRecordResponseDto dto = cloverMissionRecordService.updateMissionRecord(TEST_MEMBER_ID, request);

			// Then
			assertThat(dto.getImageUrl()).isEqualTo("https://s3/new_image_url.jpg");
		}

		@Test
		@DisplayName("실패 - 포토 타입 빈 이미지 문자열")
		void updateMissionRecord_fail_emptyImage() {
			// Given
			CloverMissionRecord record = createCompletedRecord(CloverType.PHOTO);
			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.of(record));
			CloverMissionRecordRequestDto request = buildRequest(TEST_USER_MISSION_ID, "사진 수정", MissionDifficulty.NORMAL, " ");

			// When & Then
			CustomException exception = assertThrows(CustomException.class, () -> cloverMissionRecordService.updateMissionRecord(TEST_MEMBER_ID, request));
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.IMAGE_URL_REQUIRED);
		}

		@Test
		@DisplayName("실패 - 존재하지 않는 미션 기록")
		void updateMissionRecord_notFound() {
			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.empty());
			CloverMissionRecordRequestDto request = buildRequest(TEST_USER_MISSION_ID, "x", MissionDifficulty.EASY, null);
			CustomException exception = assertThrows(CustomException.class, () -> cloverMissionRecordService.updateMissionRecord(TEST_MEMBER_ID, request));
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);
		}

		@Test
		@DisplayName("실패 - 다른 사용자의 미션 기록")
		void updateMissionRecord_forbidden() {
			Member other = Member.builder().email("other@example.com").oauthId("oauth-2").role(Role.USER).profile(new Profile("Other","u")).gender(Gender.MALE).build();
			ReflectionTestUtils.setField(other, "id", 99L);
			CloverMissionRecord record = CloverMissionRecord.builder()
					.id(TEST_USER_MISSION_ID)
					.member(other)
					.missionId(100L)
					.missionTitle("테스트 미션")
					.cloverMissionStatus(CloverMissionStatus.COMPLETED)
					.cloverType(CloverType.TIMER)
					.build();
			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.of(record));
			CloverMissionRecordRequestDto request = buildRequest(TEST_USER_MISSION_ID, "comment", MissionDifficulty.EASY, null);

			CustomException exception = assertThrows(CustomException.class, () -> cloverMissionRecordService.updateMissionRecord(TEST_MEMBER_ID, request));
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_FORBIDDEN);
		}

		@Test
		@DisplayName("실패 - 완료 상태가 아닌 미션 수정 불가")
		void updateMissionRecord_invalidStatus() {

			CloverMissionRecord record = createStartedRecord(CloverType.TIMER);

			given(missionRecordRepository.findByIdWithMember(TEST_USER_MISSION_ID)).willReturn(Optional.of(record));
			CloverMissionRecordRequestDto request = buildRequest(TEST_USER_MISSION_ID, "comment", MissionDifficulty.EASY, null);

			CustomException exception = assertThrows(CustomException.class, () -> cloverMissionRecordService.updateMissionRecord(TEST_MEMBER_ID, request));
			assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MISSION_STATUS);
		}
	}
}



