package com.example.live_backend.domain.clover.repository;

import com.example.live_backend.domain.clover.entity.MissionUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CloverMissionRepository extends JpaRepository<MissionUser,Long> {

    /**
     * 특정 사용자의 오늘 만들어진 클로버 미션들(3개) 조회
     */
    @Query("SELECT mu FROM MissionUser mu " +
            "WHERE mu.user.id = :userId " +
            "AND mu.missionDefault IS NOT NULL " +
            "AND DATE(mu.createdAt) = DATE(:today)")
    List<MissionUser> findTodayCloverMissionsByUserId(
            @Param("userId") Long userId,
            @Param("today") LocalDateTime today
    );
}
