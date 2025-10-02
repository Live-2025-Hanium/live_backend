package com.example.live_backend.domain.clover.service;

import com.example.live_backend.domain.clover.dto.CloverResponseDto;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.example.live_backend.global.constant.CloverConstants.CLOVER_MISSION_REWARD;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloverService 테스트")
class CloverServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CloverService cloverService;

    private Member member;

    private static final Long TEST_MEMBER_ID = 1L;
    private static final int INITIAL_CLOVER_COUNT = 5;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .oauthId("test-oauth-id")
                .email("test@example.com")
                .profile(Profile.builder()
                        .nickname("테스트유저")
                        .profileImageUrl("http://test.com/image.jpg")
                        .build())
                .build();
        ReflectionTestUtils.setField(member, "id", TEST_MEMBER_ID);
        ReflectionTestUtils.setField(member, "cloverCount", INITIAL_CLOVER_COUNT);
    }

    @Nested
    @DisplayName("클로버 개수 조회 기능")
    class GetCloverCountTest {

        @Test
        @DisplayName("성공 - 정상적으로 클로버 개수 조회")
        void getCloverCount_Success() {
            // given
            given(memberRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.of(member));

            // when
            CloverResponseDto responseDto = cloverService.getCloverCount(TEST_MEMBER_ID);

            // then
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.cloverCount()).isEqualTo(INITIAL_CLOVER_COUNT);
            verify(memberRepository).findById(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("성공 - 클로버 개수가 0인 경우")
        void getCloverCount_WhenCountIsZero_Success() {
            // given
            ReflectionTestUtils.setField(member, "cloverCount", 0);
            given(memberRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.of(member));

            // when
            CloverResponseDto responseDto = cloverService.getCloverCount(TEST_MEMBER_ID);

            // then
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.cloverCount()).isEqualTo(0);
            verify(memberRepository).findById(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("성공 - 클로버 개수가 많은 경우")
        void getCloverCount_WhenCountIsLarge_Success() {
            // given
            int largeCount = 9999;
            ReflectionTestUtils.setField(member, "cloverCount", largeCount);
            given(memberRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.of(member));

            // when
            CloverResponseDto responseDto = cloverService.getCloverCount(TEST_MEMBER_ID);

            // then
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.cloverCount()).isEqualTo(largeCount);
            verify(memberRepository).findById(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회원 ID")
        void getCloverCount_WhenMemberNotFound_ThrowException() {
            // given
            Long invalidMemberId = 999L;
            given(memberRepository.findById(invalidMemberId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cloverService.getCloverCount(invalidMemberId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verify(memberRepository).findById(invalidMemberId);
        }

        @Test
        @DisplayName("실패 - null 회원 ID로 조회 시도")
        void getCloverCount_WhenMemberIdIsNull_ThrowException() {
            // given
            given(memberRepository.findById(null))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cloverService.getCloverCount(null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verify(memberRepository).findById(null);
        }

        @Test
        @DisplayName("성공 - 다른 회원의 클로버 조회")
        void getCloverCount_ForDifferentMember_Success() {
            // given
            Long anotherMemberId = 2L;
            int anotherCloverCount = 10;
            Member anotherMember = Member.builder()
                    .oauthId("another-oauth-id")
                    .email("another@example.com")
                    .profile(Profile.builder()
                            .nickname("다른유저")
                            .profileImageUrl("http://test.com/another.jpg")
                            .build())
                    .build();
            ReflectionTestUtils.setField(anotherMember, "id", anotherMemberId);
            ReflectionTestUtils.setField(anotherMember, "cloverCount", anotherCloverCount);

            given(memberRepository.findById(anotherMemberId))
                    .willReturn(Optional.of(anotherMember));

            // when
            CloverResponseDto responseDto = cloverService.getCloverCount(anotherMemberId);

            // then
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.cloverCount()).isEqualTo(anotherCloverCount);
            verify(memberRepository).findById(anotherMemberId);
        }

        @Test
        @DisplayName("성공 - ResponseDto가 올바른 값을 포함하는지 검증")
        void getCloverCount_ResponseDtoContainsCorrectValue() {
            // given
            int expectedCount = 42;
            ReflectionTestUtils.setField(member, "cloverCount", expectedCount);
            given(memberRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.of(member));

            // when
            CloverResponseDto responseDto = cloverService.getCloverCount(TEST_MEMBER_ID);

            // then
            assertThat(responseDto.cloverCount()).isEqualTo(expectedCount);
            assertThat(responseDto).extracting("cloverCount").isEqualTo(expectedCount);
            verify(memberRepository).findById(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("성공 - 클로버 미션 완료 후 증가된 개수 조회")
        void getCloverCount_AfterCloverMissionComplete_Success() {
            // given
            member.increaseCloverCount(CLOVER_MISSION_REWARD);
            given(memberRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.of(member));

            // when
            CloverResponseDto responseDto = cloverService.getCloverCount(TEST_MEMBER_ID);

            // then
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.cloverCount()).isEqualTo(INITIAL_CLOVER_COUNT + 1);
            verify(memberRepository).findById(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("성공 - 여러 번 조회해도 동일한 값 반환")
        void getCloverCount_MultipleCallsReturnSameValue_Success() {
            // given
            given(memberRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.of(member));

            // when
            CloverResponseDto firstCall = cloverService.getCloverCount(TEST_MEMBER_ID);
            CloverResponseDto secondCall = cloverService.getCloverCount(TEST_MEMBER_ID);

            // then
            assertThat(firstCall.cloverCount()).isEqualTo(secondCall.cloverCount());
            assertThat(firstCall.cloverCount()).isEqualTo(INITIAL_CLOVER_COUNT);
            verify(memberRepository, times(2)).findById(TEST_MEMBER_ID);
        }
    }
}