package com.example.live_backend.domain.mission.my.repository;

import com.example.live_backend.domain.mission.my.entity.MyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyMissionRepository extends JpaRepository<MyMission, Long> {

    List<MyMission> findAllByMemberId(Long memberId);
}
