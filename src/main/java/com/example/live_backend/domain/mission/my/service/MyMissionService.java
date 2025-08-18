package com.example.live_backend.domain.mission.my.service;

import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.mission.my.dto.MyMissionRecordResponseDto;
import com.example.live_backend.domain.mission.my.dto.MyMissionRequestDto;
import com.example.live_backend.domain.mission.my.dto.MyMissionResponseDto;
import com.example.live_backend.domain.mission.my.entity.MyMission;
import com.example.live_backend.domain.mission.my.entity.MyMissionRecord;
import com.example.live_backend.domain.mission.my.repository.MyMissionRecordRepository;
import com.example.live_backend.domain.mission.my.repository.MyMissionRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyMissionService {

    private final MemberRepository memberRepository;
    private final MyMissionRepository myMissionRepository;
    private final MyMissionRecordRepository myMissionRecordRepository;

    @Transactional
    public MyMissionResponseDto createMyMission(MyMissionRequestDto requestDto, Long userId) {

        Member member = this.memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        MyMission myMission = MyMission.from(requestDto, member);

        MyMission savedMission = myMissionRepository.save(myMission);

        return MyMissionResponseDto.from(savedMission);
    }

    @Transactional
    public MyMissionResponseDto updateMyMission(Long myMissionId, MyMissionRequestDto requestDto, Long memberId) {

        MyMission myMission = myMissionRepository.findById(myMissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if (!myMission.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.MISSION_UPDATE_DENIED);
        }

        myMission.update(requestDto);

        return MyMissionResponseDto.from(myMission);
    }


    @Transactional
    public void deleteMyMission(Long myMissionId, Long memberId) {

        MyMission myMission = myMissionRepository.findById(myMissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if (!myMission.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.MISSION_DELETE_DENIED);
        }

        myMissionRepository.delete(myMission);
    }

    public List<MyMissionResponseDto> getMyMissionsList(Long memberId) {

        List<MyMission> myMissions = myMissionRepository.findAllByMemberId(memberId);

        return myMissions.stream()
                .map(MyMissionResponseDto::from)
                .toList();
    }

    @Transactional
    public List<MyMissionRecordResponseDto> getTodayMissions(Long memberId) {

        LocalDate today = LocalDate.now();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<MyMissionRecord> existingMyMissionRecords =
                myMissionRecordRepository.findByMemberAndAssignedDate(member, today);

        Set<Long> existingMyMissionIds = existingMyMissionRecords.stream()
                .map(myMissionRecord -> myMissionRecord.getMyMission().getId())
                .collect(Collectors.toSet());

        List<MyMissionRecord> newMyMissionRecords = myMissionRepository.findAllByMemberId(memberId).stream()
                .filter(this::isMissionForToday)
                .filter(myMission -> !existingMyMissionIds.contains(myMission.getId()))
                .map(newMyMission -> MyMissionRecord.from(newMyMission, member))
                .toList();

        if (!newMyMissionRecords.isEmpty()) {
            myMissionRecordRepository.saveAll(newMyMissionRecords);
        }

        List<MyMissionRecord> results = myMissionRecordRepository.findByMemberAndAssignedDate(member, today);

        return results.stream()
                .map(MyMissionRecordResponseDto::from)
                .toList();
    }

    private boolean isMissionForToday(MyMission mission) {
        LocalDate today = LocalDate.now();
        DayOfWeek todayOfWeek = today.getDayOfWeek();

        boolean isDateValid = !mission.getStartDate().isAfter(today) &&
                 !mission.getEndDate().isBefore(today);

        boolean isRepeatDayValid = mission.getRepeatDays().isEmpty() ||
                mission.getRepeatDays().contains(todayOfWeek);

        return mission.isActive() && isDateValid && isRepeatDayValid;
    }

    @Transactional
    public MyMissionRecordResponseDto completeMyMission(Long userMissionId, Long memberId) {

        LocalDate today = LocalDate.now();

        MyMissionRecord myMissionRecord = myMissionRecordRepository.findById(userMissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        if (today.isAfter(myMissionRecord.getAssignedDate())) {
            throw new CustomException(ErrorCode.MISSION_EXPIRED);
        }

        if (!myMissionRecord.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.MISSION_FORBIDDEN);
        }

        MyMission myMission = myMissionRepository.findById(myMissionRecord.getMyMission().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        myMissionRecord.completeMission();

        return MyMissionRecordResponseDto.from(myMissionRecord);
    }
}
