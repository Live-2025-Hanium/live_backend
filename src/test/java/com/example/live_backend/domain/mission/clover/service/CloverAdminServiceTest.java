package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.mission.clover.Enum.CloverType;
import com.example.live_backend.domain.mission.clover.Enum.MissionCategory;
import com.example.live_backend.domain.mission.clover.Enum.MissionDifficulty;
import com.example.live_backend.domain.mission.clover.Enum.TargetUserType;
import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionRequestDto;
import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionCreateRequestDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionVectorDataDto;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

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
    private VectorStore vectorStore;

    @InjectMocks
    private CloverAdminService cloverAdminService;

    private AdminRegisterCloverMissionRequestDto buildRequest(CloverType type) {
        CloverMissionCreateRequestDto create = new CloverMissionCreateRequestDto();
        ReflectionTestUtils.setField(create, "missionTitle", "걷기 미션");
        ReflectionTestUtils.setField(create, "description", "하루 10분 걷기");
        ReflectionTestUtils.setField(create, "missionCategory", MissionCategory.HEALTH);
        ReflectionTestUtils.setField(create, "missionDifficulty", MissionDifficulty.EASY);
        ReflectionTestUtils.setField(create, "cloverType", type);
        ReflectionTestUtils.setField(create, "requiredSeconds", 600);
        ReflectionTestUtils.setField(create, "requiredMeters", 1000);
        ReflectionTestUtils.setField(create, "illustrationUrl", "https://image");
        ReflectionTestUtils.setField(create, "targetAddress", "서울시 강남구");

        CloverMissionVectorDataDto vector = new CloverMissionVectorDataDto();
        ReflectionTestUtils.setField(vector, "targetUserType", TargetUserType.HEALTH_VULNERABLE);
        ReflectionTestUtils.setField(vector, "relatedFeature", "건강 취약층");
        ReflectionTestUtils.setField(vector, "activityDescription", "도보 활동을 장려");
        ReflectionTestUtils.setField(vector, "expectedEffect", "심폐지구력 향상");

        AdminRegisterCloverMissionRequestDto request = AdminRegisterCloverMissionRequestDto.builder()
                .cloverMissionCreateRequestDto(create)
                .cloverMissionVectorDataDto(vector)
                .build();
        return request;
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

            given(cloverMissionRepository.save(any(CloverMission.class))).willAnswer(invocation -> {
                CloverMission arg = invocation.getArgument(0);
                ReflectionTestUtils.setField(arg, "id", 10L);
                return arg;
            });

            // When
            AdminRegisterCloverMissionResponseDto response = cloverAdminService.registerCloverMission(request);

            // Then
            verify(cloverMissionRepository, times(1)).save(missionCaptor.capture());
            CloverMission savedArg = missionCaptor.getValue();
            assertThat(savedArg.getTitle()).isEqualTo("걷기 미션");
            assertThat(savedArg.getDescription()).isEqualTo("하루 10분 걷기");
            assertThat(savedArg.getCategory()).isEqualTo(MissionCategory.HEALTH);
            assertThat(savedArg.getDifficulty()).isEqualTo(MissionDifficulty.EASY);

            // Then
            ArgumentCaptor<List<Document>> docsCaptor = ArgumentCaptor.forClass(List.class);
            verify(vectorStore, times(1)).add(docsCaptor.capture());
            List<Document> docs = docsCaptor.getValue();
            assertThat(docs).hasSize(1);
            Document doc = docs.get(0);
            String expectedVectorDocument = String.format(
                    "미션 제목: %s, 미션 설명: %s, 도움을 줄 수 있는 사용자의 특성: %s, 기대 효과: %s",
                    "걷기 미션", "도보 활동을 장려", "건강 취약층", "심폐지구력 향상");

            Map<String, Object> metadata = doc.getMetadata();
            assertThat(metadata.get("clover_mission_id")).isEqualTo("10");
            assertThat(metadata.get("mission_title")).isEqualTo("걷기 미션");
            assertThat(metadata.get("mission_category")).isEqualTo(MissionCategory.HEALTH.name());
            assertThat(metadata.get("mission_difficulty")).isEqualTo(MissionDifficulty.EASY.name());
            assertThat(metadata.get("target_user_type")).isEqualTo(TargetUserType.HEALTH_VULNERABLE.name());

            assertThat(response.getCloverMissionId()).isEqualTo(10L);
            assertThat(response.getMissionTitle()).isEqualTo("걷기 미션");
            assertThat(response.getDescription()).isEqualTo("하루 10분 걷기");
            assertThat(response.getMissionCategory()).isEqualTo(MissionCategory.HEALTH);
            assertThat(response.getMissionDifficulty()).isEqualTo(MissionDifficulty.EASY);
            assertThat(response.getTargetUserType()).isEqualTo(TargetUserType.HEALTH_VULNERABLE);
            assertThat(response.getVectorDocument()).isEqualTo(expectedVectorDocument);
        }
    }

    @Nested
    @DisplayName("클로버 미션 등록 실패 케이스")
    class RegisterFailure {
        @Test
        @DisplayName("저장 중 예외 발생 시 예외 전파 및 vectorStore 미호출")
        void register_fail_repository() {

            // Given
            AdminRegisterCloverMissionRequestDto request = buildRequest(CloverType.DISTANCE);
            given(cloverMissionRepository.save(any(CloverMission.class)))
                    .willThrow(new RuntimeException("DB error"));

            // When & Then
            assertThrows(RuntimeException.class, () -> cloverAdminService.registerCloverMission(request));
            verify(vectorStore, never()).add(anyList());
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

            doThrow(new RuntimeException("Vector error")).when(vectorStore).add(anyList());

            // When & Then
            assertThrows(RuntimeException.class, () -> cloverAdminService.registerCloverMission(request));
            verify(cloverMissionRepository, times(1)).save(any(CloverMission.class));
        }

        @Test
        @DisplayName("지원하지 않는 CloverType 이면 INVALID_CLOVER_TYPE 예외")
        void register_fail_invalid_type() {
            // Given
            AdminRegisterCloverMissionRequestDto request = buildRequest(null);

            // When & Then
            assertThrows(Exception.class, () -> cloverAdminService.registerCloverMission(request));
            verify(cloverMissionRepository, never()).save(any());
            verify(vectorStore, never()).add(anyList());
        }
    }
}


