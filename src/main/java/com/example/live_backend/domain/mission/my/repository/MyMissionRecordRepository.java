package com.example.live_backend.domain.mission.my.repository;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.mission.my.entity.MyMissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MyMissionRecordRepository  extends JpaRepository<MyMissionRecord, Long> {

    List<MyMissionRecord> findByMemberAndAssignedDate(Member member, LocalDate today);
}
