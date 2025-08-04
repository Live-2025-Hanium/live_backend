package com.example.live_backend.domain.mission.repository;

import com.example.live_backend.domain.mission.entity.CloverMission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloverMissionRepository extends JpaRepository<CloverMission, Long> {
}
