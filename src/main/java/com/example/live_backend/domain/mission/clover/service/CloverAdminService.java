package com.example.live_backend.domain.mission.clover.service;

import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionRequestDto;
import com.example.live_backend.domain.mission.clover.dto.AdminRegisterCloverMissionResponseDto;
import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionRepository;
import com.example.live_backend.domain.mission.clover.repository.CloverMissionVectorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


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
