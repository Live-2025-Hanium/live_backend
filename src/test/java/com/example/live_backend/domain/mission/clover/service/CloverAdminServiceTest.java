package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionRequestDto;
import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionResponseDto;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRepository;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionVectorRepository;
import com.example.live_backend.global.error.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloverAdminService 테스트")
class CloverAdminServiceTest {

    @Mock
    private CloverMissionRepository cloverMissionRepository;

    @Mock
    private CloverMissionVectorRepository cloverMissionVectorRepository;

    @InjectMocks
    private CloverAdminService cloverAdminService;

    private AdminRegisterCloverMissionRequestDto buildRequest(CloverType type) {
        return AdminRegisterCloverMissionRequestDto.builder()
                .missionTitle("걷기 미션")
                .description("하루 10분 걷기")
                .missionCategory(MissionCategory.HEALTH)
                .missionDifficulty(MissionDifficulty.EASY)
                .cloverType(type)
                .requiredSeconds(600)
                .requiredMeters(1000)
                .illustrationUrl("https://image.url/photo.jpg")
                .targetAddress("서울시 강남구 테헤란로")
                .relatedFeature("건강 취약층")
                .activityDescription("도보 활동을 장려")
                .expectedEffect("심폐지구력 향상")
                .build();
    }

    @Nested
    @DisplayName("클로버 미션 등록 성공 케이스")
    class RegisterSuccess {

        @Test
        @DisplayName("정상 등록 및 벡터 저장, 응답 매핑 확인")
        void register_success_basic() {

            // Given
            AdminRegisterCloverMissionRequestDto request = buildRequest(CloverType.TIMER);

            ArgumentCaptor<CloverMission> missionCaptor = ArgumentCaptor.forClass(CloverMission.class);

            String mockVectorDocument = "미션 제목: 걷기 미션, 미션 설명: 도보 활동을 장려, ...";

            given(cloverMissionRepository.save(any(CloverMission.class))).willAnswer(invocation -> {
                CloverMission arg = invocation.getArgument(0);
                ReflectionTestUtils.setField(arg, "id", 10L);
                return arg;
            });

            given(cloverMissionVectorRepository.saveMissionToVectorDB(any(), anyString(), anyString(), anyString()))
                    .willReturn(mockVectorDocument);

            // When
            AdminRegisterCloverMissionResponseDto response = cloverAdminService.registerCloverMission(request);

            // Then
            verify(cloverMissionRepository, times(1)).save(missionCaptor.capture());

            CloverMission savedArg = missionCaptor.getValue();
            assertThat(savedArg.getTitle()).isEqualTo("걷기 미션");
            assertThat(savedArg.getDescription()).isEqualTo("하루 10분 걷기");

            verify(cloverMissionVectorRepository, times(1)).saveMissionToVectorDB(
                    eq(savedArg),
                    eq(request.getActivityDescription()),
                    eq(request.getRelatedFeature()),
                    eq(request.getExpectedEffect())
            );

            assertThat(response.getCloverMissionId()).isEqualTo(10L);
            assertThat(response.getMissionTitle()).isEqualTo("걷기 미션");
            assertThat(response.getVectorDocument()).isEqualTo(mockVectorDocument);
        }
    }

    @Nested
    @DisplayName("클로버 미션 등록 실패 케이스")
    class RegisterFailure {
        @Test
        @DisplayName("DB 저장 중 예외 발생 시 예외 전파 및 vectorRepository 미호출")
        void register_fail_repository() {

            // Given
            AdminRegisterCloverMissionRequestDto request = buildRequest(CloverType.DISTANCE);
            given(cloverMissionRepository.save(any(CloverMission.class)))
                    .willThrow(new RuntimeException("DB error"));

            // When & Then
            assertThrows(RuntimeException.class, () -> cloverAdminService.registerCloverMission(request));

            verify(cloverMissionVectorRepository, never()).saveMissionToVectorDB(any(), any(), any(), any());
        }

        @Test
        @DisplayName("벡터 저장 중 예외 발생 시 예외 전파")
        void register_fail_vector() {

            // Given
            AdminRegisterCloverMissionRequestDto request = buildRequest(CloverType.PHOTO);

            given(cloverMissionRepository.save(any(CloverMission.class))).willAnswer(invocation -> {
                CloverMission arg = invocation.getArgument(0);
                ReflectionTestUtils.setField(arg, "id", 11L);
                return arg;
            });
            // vectorStore.add() 대신 vectorRepository의 메서드를 Mocking
            given(cloverMissionVectorRepository.saveMissionToVectorDB(any(), anyString(), anyString(), anyString()))
                    .willThrow(new RuntimeException("Vector error"));

            // When & Then
            assertThrows(RuntimeException.class, () -> cloverAdminService.registerCloverMission(request));
            verify(cloverMissionRepository, times(1)).save(any(CloverMission.class));
        }

        @Test
        @DisplayName("지원하지 않는 CloverType 이면 CustomException 예외 발생")
        void register_fail_invalid_type() {
            // Given
            AdminRegisterCloverMissionRequestDto request = buildRequest(null);

            // When & Then
            assertThrows(CustomException.class, () -> cloverAdminService.registerCloverMission(request));
            verify(cloverMissionRepository, never()).save(any());
            verify(cloverMissionVectorRepository, never()).saveMissionToVectorDB(any(), any(), any(), any());
        }
    }
}


