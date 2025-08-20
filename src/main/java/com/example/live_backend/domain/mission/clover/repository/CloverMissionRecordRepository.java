package com.example.live_backend.domain.mission.clover.repository;

import com.example.live_backend.domain.mission.clover.Enum.CloverMissionStatus;
import com.example.live_backend.domain.mission.clover.entity.CloverMissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Query("SELECT COUNT(cmr) FROM CloverMissionRecord cmr " +
            "WHERE cmr.member.id = :memberId AND cmr.assignedDate BETWEEN :start AND :end")
    long countAssignedInPeriod(@Param("memberId") Long memberId,
                               @Param("start") LocalDate start,
                               @Param("end") LocalDate end);

    @Query("SELECT COUNT(cmr) FROM CloverMissionRecord cmr " +
            "WHERE cmr.member.id = :memberId AND cmr.cloverMissionStatus = :status " +
            "AND cmr.completedAt BETWEEN :start AND :end")
    long countCompletedInPeriod(@Param("memberId") Long memberId,
                                @Param("status") CloverMissionStatus status,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    @Query("SELECT cmr FROM CloverMissionRecord cmr " +
            "WHERE cmr.member.id = :memberId AND cmr.cloverMissionStatus = :status " +
            "AND cmr.completedAt BETWEEN :start AND :end ORDER BY cmr.completedAt ASC")
    List<CloverMissionRecord> findCompletedInPeriod(@Param("memberId") Long memberId,
                                                    @Param("status") CloverMissionStatus status,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    @Query("SELECT cmr FROM CloverMissionRecord cmr " +
            "WHERE cmr.member.id = :memberId AND cmr.cloverMissionStatus = :status " +
            "AND DATE(cmr.completedAt) = DATE(:date) ORDER BY cmr.completedAt ASC")
    List<CloverMissionRecord> findCompletedOnDate(@Param("memberId") Long memberId,
                                                  @Param("status") CloverMissionStatus status,
                                                  @Param("date") LocalDate date);

    @Query("SELECT cmr.missionCategory, COUNT(cmr) FROM CloverMissionRecord cmr " +
            "WHERE cmr.member.id = :memberId AND cmr.cloverMissionStatus = :status " +
            "AND cmr.completedAt BETWEEN :start AND :end " +
            "GROUP BY cmr.missionCategory")
    List<Object[]> countCompletedByCategoryInPeriod(@Param("memberId") Long memberId,
                                                    @Param("status") CloverMissionStatus status,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

}