package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionRequestDto;
import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionResponseDto;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRepository;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionVectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CloverAdminService {

    private final CloverMissionRepository cloverMissionRepository;
    private final CloverMissionVectorRepository cloverMissionVectorRepository;

    @Transactional
    public AdminRegisterCloverMissionResponseDto registerCloverMission(AdminRegisterCloverMissionRequestDto request) {

        CloverMission newMission = CloverMission.from(request);
        CloverMission savedMission = cloverMissionRepository.save(newMission);

        String vectorDocument = cloverMissionVectorRepository.saveMissionToVectorDB(
                savedMission,
                request.getActivityDescription(),
                request.getRelatedFeature(),
                request.getExpectedEffect()
        );

        return AdminRegisterCloverMissionResponseDto.from(savedMission, vectorDocument);
    }
}
