package com.example.live_backend.domain.mission.my.repository;

import com.example.live_backend.domain.mission.my.entity.MyMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MyMissionRepository extends JpaRepository<MyMission, Long> {

    List<MyMission> findAllByMemberId(Long memberId);

    @Query("SELECT m FROM MyMission m WHERE m.member.id = :memberId AND :currentDate BETWEEN m.startDate AND m.endDate")
    List<MyMission> findMissionsByDate(
            @Param("memberId") Long memberId,
            @Param("currentDate") LocalDate currentDate
    );

}
