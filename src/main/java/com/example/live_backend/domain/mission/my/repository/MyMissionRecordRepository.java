package com.example.live_backend.domain.mission.my.repository;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.mission.my.Enum.MyMissionStatus;
import com.example.live_backend.domain.mission.my.entity.MyMissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MyMissionRecordRepository  extends JpaRepository<MyMissionRecord, Long> {

    List<MyMissionRecord> findByMemberAndAssignedDate(Member member, LocalDate today);

    @Query("SELECT COUNT(mmr) FROM MyMissionRecord mmr " +
            "WHERE mmr.member.id = :memberId AND mmr.assignedDate BETWEEN :start AND :end")
    long countAssignedInPeriod(@Param("memberId") Long memberId,
                               @Param("start") LocalDate start,
                               @Param("end") LocalDate end);

    @Query("SELECT COUNT(mmr) FROM MyMissionRecord mmr " +
            "WHERE mmr.member.id = :memberId AND mmr.myMissionStatus = :status " +
            "AND mmr.completedAt BETWEEN :start AND :end")
    long countCompletedInPeriod(@Param("memberId") Long memberId,
                                @Param("status") MyMissionStatus status,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    @Query("SELECT mmr FROM MyMissionRecord mmr " +
            "WHERE mmr.member.id = :memberId AND mmr.myMissionStatus = :status " +
            "AND mmr.completedAt BETWEEN :start AND :end ORDER BY mmr.completedAt ASC")
    List<MyMissionRecord> findCompletedInPeriod(@Param("memberId") Long memberId,
                                                @Param("status") MyMissionStatus status,
                                                @Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);
}
