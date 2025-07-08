package com.example.live_backend.domain.clover.service;

import com.example.live_backend.domain.clover.dto.CloverMissionListResponseDto;
import com.example.live_backend.domain.clover.dto.CloverMissionResponseDto;
import com.example.live_backend.domain.clover.entity.MissionDefault;
import com.example.live_backend.domain.clover.entity.MissionUser;
import com.example.live_backend.domain.clover.repository.CloverMissionRepository;
import com.example.live_backend.domain.example.entity.User;
import com.example.live_backend.domain.memeber.util.UserUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MissionService 테스트")
class MissionServiceTest {

    @Mock
    private CloverMissionRepository cloverMissionRepository;

    @Mock
    private UserUtil userUtil;

    @InjectMocks
    private MissionService missionService;

    @Test
    @DisplayName("클로버 미션 목록 조회 성공")
    void getCloverMissionList_Success() {

        // given
        Long userId = 1L;

        User testUser = User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .build();

        ReflectionTestUtils.setField(testUser, "id", userId);

        MissionDefault mockMissionDefault = MissionDefault.builder().build();

        // Mock MissionUser 객체 생성
        MissionUser missionUser1 = createMockMissionUser(101L, "친구에게 칭찬 한마디 건네기", testUser, mockMissionDefault);
        MissionUser missionUser2 = createMockMissionUser(102L, "출근길에 하늘 사진 찍기", testUser, mockMissionDefault);
        MissionUser missionUser3 = createMockMissionUser(103L, "부모님께 안부 인사 전하기", testUser, mockMissionDefault);

        List<MissionUser> mockMissions = Arrays.asList(missionUser1, missionUser2, missionUser3);

        // Mock 객체의 동작 정의
        when(userUtil.getCurrentUserId()).thenReturn(userId); // userUtil.getCurrentUserId()가 호출되면 userId (1L)를 반환하도록 설정.
        when(cloverMissionRepository.findTodayCloverMissionsByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(mockMissions); // cloverMissionRepository.findTodayCloverMissionsByUserId(...)가 호출되면 미리 만들어 둔 mockMissions 리스트를 반환하도록 설정.

        // when (실제 테스트 대상인 missionService.getCloverMissionList() 메소드를 호출)
        CloverMissionListResponseDto responseDto = missionService.getCloverMissionList();

        // then (결과 확인)
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getUserId()).isEqualTo(userId);

        CloverMissionListResponseDto.TodayMissionDto missionDto1 = responseDto.getMissions().get(0);
        assertThat(missionDto1.getMissionId()).isEqualTo(101L);
        assertThat(missionDto1.getTitle()).isEqualTo("친구에게 칭찬 한마디 건네기");

        CloverMissionListResponseDto.TodayMissionDto missionDto2 = responseDto.getMissions().get(1);
        assertThat(missionDto2.getMissionId()).isEqualTo(102L);
        assertThat(missionDto2.getTitle()).isEqualTo("출근길에 하늘 사진 찍기");

        CloverMissionListResponseDto.TodayMissionDto missionDto3 = responseDto.getMissions().get(2);
        assertThat(missionDto3.getMissionId()).isEqualTo(103L);
        assertThat(missionDto3.getTitle()).isEqualTo("부모님께 안부 인사 전하기");
    }

    /**
     * 테스트용 MissionUser 객체를 생성하는 헬퍼 메소드
     */
    private MissionUser createMockMissionUser(Long missionId, String title, User user, MissionDefault missionDefault) {
        MissionUser missionUser = new MissionUser(user, missionDefault, title, "작은 칭찬으로 친구의 하루를 특별하게 만들어주세요.", null);
        // ReflectionTestUtils 를 사용해 private 필드에 값을 설정
        ReflectionTestUtils.setField(missionUser, "id", missionId);
        ReflectionTestUtils.setField(missionUser, "title", title);
        ReflectionTestUtils.setField(missionUser, "user", user); // 연관관계 설정
        return missionUser;
    }

    @Test
    @DisplayName("1개의 클로버 미션 상세 조회 성공")
    void getCloverMissionDetail_Success() {
        // given
        Long userId = 1L;
        Long missionId = 101L;

        User testUser = User.builder()
                .nickname("테스트유저").build();

        ReflectionTestUtils.setField(testUser, "id", userId);

        // 상세 조회를 위해 MissionDefault 객체 생성
        MissionDefault missionDefault = MissionDefault.builder()
                .title("친구에게 칭찬 한마디 건네기")
                .description("작은 칭찬으로 친구의 하루를 특별하게 만들어주세요.")
                .category(MissionDefault.Category.RELATIONSHIP)
                .difficulty(MissionDefault.Difficulty.EASY)
                .build();
        ReflectionTestUtils.setField(missionDefault, "id", 1L);

        // 테스트 대상 MissionUser 객체 생성
        MissionUser mockMission = createMockMissionUser(missionId, "친구에게 칭찬 한마디 건네기", testUser, missionDefault);

        // Mock 객체 동작 정의
        when(cloverMissionRepository.findCloverMissionById(missionId)).thenReturn(mockMission);

        // when
        // 서비스의 상세 조회 메소드 호출
        CloverMissionResponseDto responseDto = missionService.getCloverMission(missionId);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getMissionId()).isEqualTo(missionId);
        assertThat(responseDto.getTitle()).isEqualTo("친구에게 칭찬 한마디 건네기");
        assertThat(responseDto.getDescription()).isEqualTo("작은 칭찬으로 친구의 하루를 특별하게 만들어주세요.");
        assertThat(responseDto.getCategory()).isEqualTo(MissionDefault.Category.RELATIONSHIP);
        assertThat(responseDto.getDifficulty()).isEqualTo(MissionDefault.Difficulty.EASY);
    }
}