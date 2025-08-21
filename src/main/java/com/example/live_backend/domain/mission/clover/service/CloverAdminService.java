package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionRequestDto;
import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionResponseDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionCreateRequestDto;
import com.example.live_backend.domain.mission.clover.dto.CloverMissionVectorDataDto;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRepository;
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
    public AdminRegisterCloverMissionResponseDto registerCloverMission(AdminRegisterCloverMissionRequestDto request) {

        CloverMissionCreateRequestDto createDto = request.getCloverMissionCreateRequestDto();
        CloverMissionVectorDataDto vectorDto = request.getCloverMissionVectorDataDto();

        CloverMission newMission = CloverMission.from(createDto);

        CloverMission savedMission = cloverMissionRepository.save(newMission);

        // document로 저장할 텍스트 생성
        // document로 저장할 텍스트 생성
        String vectorDocument = String.format(
                "미션 제목: %s, 미션 설명: %s, 도움을 줄 수 있는 사용자의 특성: %s, 기대 효과: %s",
                createDto.getMissionTitle(),
                vectorDto.getActivityDescription(),
                vectorDto.getRelatedFeature(),
                vectorDto.getExpectedEffect()
        );

        // metadata 생성
        Map<String, Object> metadata = Map.of(
                "clover_mission_id", String.valueOf(savedMission.getId()),
                "mission_title", savedMission.getTitle(),
                "mission_category", savedMission.getCategory().name(),
                "mission_difficulty", savedMission.getDifficulty().name(),
                "target_user_type", vectorDto.getTargetUserType().name()
        );

        vectorStore.add(List.of(new Document(vectorDocument, metadata)));

        return AdminRegisterCloverMissionResponseDto.builder()
                .cloverMissionId(newMission.getId())
                .missionTitle(newMission.getTitle())
                .description(newMission.getDescription())
                .missionCategory(newMission.getCategory())
                .missionDifficulty(newMission.getDifficulty())
                .targetUserType(vectorDto.getTargetUserType())
                .vectorDocument(vectorDocument)
                .build();
    }

}
