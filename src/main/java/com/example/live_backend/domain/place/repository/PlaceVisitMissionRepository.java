package com.example.live_backend.domain.place.repository;

import com.example.live_backend.domain.place.entity.PlaceVisitMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface PlaceVisitMissionRepository extends JpaRepository<PlaceVisitMission, Long> {

    @Query("SELECT pvm FROM PlaceVisitMission pvm " +
           "JOIN FETCH pvm.visitMission vm " +
           "JOIN com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord cmr " +
           "ON cmr.missionId = vm.id " +
           "WHERE cmr.member.id = :memberId " +
           "AND cmr.cloverMissionStatus IN ('ASSIGNED', 'STARTED', 'PAUSED') " +
           "AND cmr.cloverType = 'VISIT'")
    Optional<PlaceVisitMission> findActiveMissionByMemberId(@Param("memberId") Long memberId);
}