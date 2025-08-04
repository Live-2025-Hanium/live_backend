package com.example.live_backend.domain.mission.service;

import com.example.live_backend.domain.memeber.Role;
import com.example.live_backend.domain.mission.dto.AdminRegisterCloverMissionRequestDto;
import com.example.live_backend.domain.mission.dto.AdminRegisterCloverMissionResponseDto;
import com.example.live_backend.domain.mission.entity.CloverMission;
import com.example.live_backend.domain.mission.repository.CloverMissionRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloverAdminService {

    private final CloverMissionRepository cloverMissionRepository;
    private final VectorStore vectorStore;

    @Transactional
    public AdminRegisterCloverMissionResponseDto registerCloverMission(AdminRegisterCloverMissionRequestDto request, String role) {

        if (!role.equals(String.valueOf(Role.ADMIN))) {
            throw new CustomException(ErrorCode.REGISTRATION_DENIED);
        }

        CloverMission newMission = CloverMission.from(request);

        CloverMission savedMission = cloverMissionRepository.save(newMission);

        // document로 저장할 텍스트 생성
        String vectorDocument = String.format(
                "미션 제목: %s, 미션 설명: %s, 도움을 줄 수 있는 사용자의 특성: %s, 기대 효과: %s",
                request.getMissionTitle(),
                request.getActivityDescription(),
                request.getRelatedFeature(),
                request.getExpectedEffect()
        );

        // metadata 생성
        Map<String, Object> metadata = Map.of(
                "clover_mission_id", String.valueOf(savedMission.getId()),
                "mission_title", savedMission.getTitle(),
                "mission_category", savedMission.getCategory().name(),
                "mission_difficulty", savedMission.getDifficulty().name(),
                "target_user_type", request.getTargetUserType().name()
        );

        vectorStore.add(List.of(new Document(vectorDocument, metadata)));

        return AdminRegisterCloverMissionResponseDto.builder()
                .cloverMissionId(newMission.getId())
                .missionTitle(newMission.getTitle())
                .description(newMission.getDescription())
                .missionCategory(newMission.getCategory())
                .missionDifficulty(newMission.getDifficulty())
                .targetUserType(request.getTargetUserType())
                .vectorDocument(vectorDocument)
                .build();
    }

}
