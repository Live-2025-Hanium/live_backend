package com.example.live_backend.domain.places.repository;

import com.example.live_backend.domain.places.entity.PlaceVisitMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaceVisitMissionRepository extends JpaRepository<PlaceVisitMission, Long> {
    
    Optional<PlaceVisitMission> findByVisitMissionId(Long visitMissionId);
    
    @Query("SELECT pvm FROM PlaceVisitMission pvm " +
           "JOIN pvm.visitMission vm " +
           "JOIN CloverMissionRecord cmr ON cmr.cloverMission = vm " +
           "WHERE cmr.member.id = :memberId " +
           "AND cmr.cloverMissionStatus IN ('ASSIGNED', 'STARTED', 'PAUSED')")
    Optional<PlaceVisitMission> findActiveMissionByMemberId(@Param("memberId") Long memberId);
}