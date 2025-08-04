package com.example.live_backend.domain.mission.repository;

import com.example.live_backend.domain.mission.entity.MissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MissionRecordRepository extends JpaRepository<MissionRecord, Long> {

    /**
     * 특정 사용자의 오늘 만들어진 클로버 미션들(3개) 조회
     */
    @Query("SELECT mr FROM MissionRecord mr " +
            "WHERE mr.member.id = :userId " +
            "AND mr.missionType = 'CLOVER' " +
            "AND DATE(mr.assignedDate) = DATE(:today) "+
            "AND mr.missionStatus = 'ASSIGNED' " +
            "OR mr.missionStatus = 'COMPLETED' ")
    List<MissionRecord> findCloverMissions(
            @Param("userId") Long userId,
            @Param("today") LocalDateTime today
    );

    @Query("SELECT mr FROM MissionRecord mr JOIN FETCH mr.member WHERE mr.id = :id")
    Optional<MissionRecord> findByIdWithUser(@Param("id") Long id);

}
