package com.example.live_backend.domain.mission.clover.repository;

import com.example.live_backend.domain.mission.clover.entity.CloverMission;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CloverMissionRecordRepository extends JpaRepository<CloverMissionRecord, Long> {

    /**
     * 특정 사용자의 오늘 할당된 클로버 미션들 조회
     */
    @Query("SELECT cmr FROM CloverMissionRecord cmr " +
            "WHERE cmr.member.id = :userId " +
            "AND DATE(cmr.assignedDate) = DATE(:today) ")
    List<CloverMissionRecord> findCloverMissionsList(
            @Param("userId") Long userId,
            @Param("today") LocalDate today
    );

    @Query("SELECT cmr FROM CloverMissionRecord cmr JOIN FETCH cmr.member WHERE cmr.id = :id")
    Optional<CloverMissionRecord> findByIdWithMember(@Param("id") Long id);
}
