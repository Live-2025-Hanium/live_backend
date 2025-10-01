package com.example.live_backend.domain.clover.service;

import com.example.live_backend.domain.clover.dto.CloverResponseDto;
import com.example.live_backend.domain.clover.entity.Clover;
import com.example.live_backend.domain.clover.repository.CloverRepository;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloverService 테스트")
class CloverServiceTest {

    @InjectMocks
    private CloverService cloverService;

    @Mock
    private CloverRepository cloverRepository;

    private Member member;
    private Clover clover;

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

        clover = new Clover(member);
        clover.increase(INITIAL_CLOVER_COUNT);
        ReflectionTestUtils.setField(clover, "memberId", TEST_MEMBER_ID);
    }

    @Nested
    @DisplayName("클로버 개수 조회 기능")
    class GetCloverCountTest {

        @Test
        @DisplayName("성공 - 정상적으로 클로버 개수 조회")
        void getCloverCount_Success() {

            // given
            given(cloverRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.of(clover));

            // when
            CloverResponseDto responseDto = cloverService.getCloverCount(TEST_MEMBER_ID);

            // then
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.getCloverCount()).isEqualTo(INITIAL_CLOVER_COUNT);
            verify(cloverRepository).findById(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("성공 - 클로버 개수가 0인 경우")
        void getCloverCount_WhenCountIsZero_Success() {

            // given
            Clover zeroClover = new Clover(member);
            ReflectionTestUtils.setField(zeroClover, "memberId", TEST_MEMBER_ID);

            given(cloverRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.of(zeroClover));

            // when
            CloverResponseDto responseDto = cloverService.getCloverCount(TEST_MEMBER_ID);

            // then
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.getCloverCount()).isEqualTo(0);
            verify(cloverRepository).findById(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("성공 - 클로버 개수가 많은 경우")
        void getCloverCount_WhenCountIsLarge_Success() {

            // given
            int largeCount = 9999;
            Clover largeClover = new Clover(member);
            largeClover.increase(largeCount);
            ReflectionTestUtils.setField(largeClover, "memberId", TEST_MEMBER_ID);

            given(cloverRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.of(largeClover));

            // when
            CloverResponseDto responseDto = cloverService.getCloverCount(TEST_MEMBER_ID);

            // then
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.getCloverCount()).isEqualTo(largeCount);
            verify(cloverRepository).findById(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회원 ID")
        void getCloverCount_WhenMemberNotFound_ThrowException() {

            // given
            Long invalidMemberId = 999L;
            given(cloverRepository.findById(invalidMemberId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cloverService.getCloverCount(invalidMemberId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verify(cloverRepository).findById(invalidMemberId);
        }

        @Test
        @DisplayName("실패 - null 회원 ID로 조회 시도")
        void getCloverCount_WhenMemberIdIsNull_ThrowException() {

            // given
            given(cloverRepository.findById(null))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cloverService.getCloverCount(null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verify(cloverRepository).findById(null);
        }

        @Test
        @DisplayName("성공 - 다른 회원의 클로버 조회")
        void getCloverCount_ForDifferentMember_Success() {

            // given
            Long anotherMemberId = 2L;
            Member anotherMember = Member.builder()
                    .oauthId("another-oauth-id")
                    .email("another@example.com")
                    .profile(Profile.builder()
                            .nickname("다른유저")
                            .profileImageUrl("http://test.com/another.jpg")
                            .build())
                    .build();
            ReflectionTestUtils.setField(anotherMember, "id", anotherMemberId);

            Clover anotherClover = new Clover(anotherMember);
            anotherClover.increase(10);
            ReflectionTestUtils.setField(anotherClover, "memberId", anotherMemberId);

            given(cloverRepository.findById(anotherMemberId))
                    .willReturn(Optional.of(anotherClover));

            // when
            CloverResponseDto responseDto = cloverService.getCloverCount(anotherMemberId);

            // then
            assertThat(responseDto).isNotNull();
            assertThat(responseDto.getCloverCount()).isEqualTo(10);
            verify(cloverRepository).findById(anotherMemberId);
        }

        @Test
        @DisplayName("성공 - ResponseDto가 올바른 값을 포함하는지 검증")
        void getCloverCount_ResponseDtoContainsCorrectValue() {

            // given
            int expectedCount = 42;
            Clover testClover = new Clover(member);
            testClover.increase(expectedCount);
            ReflectionTestUtils.setField(testClover, "memberId", TEST_MEMBER_ID);

            given(cloverRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.of(testClover));

            // when
            CloverResponseDto responseDto = cloverService.getCloverCount(TEST_MEMBER_ID);

            // then
            assertThat(responseDto.getCloverCount()).isEqualTo(expectedCount);
            assertThat(responseDto).extracting("cloverCount").isEqualTo(expectedCount);
            verify(cloverRepository).findById(TEST_MEMBER_ID);
        }
    }
}