package com.example.live_backend.domain.clover.controller;

import com.example.live_backend.domain.clover.dto.CloverResponseDto;
import com.example.live_backend.domain.clover.service.CloverService;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloverController 테스트")
class CloverControllerTest {

    @Mock
    private CloverService cloverService;

    @InjectMocks
    private cloverController cloverController;

    private static final Long TEST_MEMBER_ID = 1L;
    private static final String TEST_OAUTH_ID = "oauth-12345";
    private static final String TEST_NICKNAME = "테스트유저";
    private static final int TEST_CLOVER_COUNT = 5;

    @Nested
    @DisplayName("GET /api/clover - 클로버 개수 조회")
    class GetCloverCountTest {

        @Test
        @DisplayName("성공 - 클로버 개수 조회")
        void getCloverCount_Success() {
            // Given
            PrincipalDetails userDetails = createPrincipalDetails();
            CloverResponseDto responseDto = new CloverResponseDto(TEST_CLOVER_COUNT);

            given(cloverService.getCloverCount(eq(TEST_MEMBER_ID)))
                    .willReturn(responseDto);

            // When
            ResponseHandler<CloverResponseDto> result = cloverController.getCloverCount(userDetails);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getCloverCount()).isEqualTo(TEST_CLOVER_COUNT);
            verify(cloverService).getCloverCount(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("성공 - 클로버 개수가 0인 경우")
        void getCloverCount_ZeroCount_Success() {
            // Given
            PrincipalDetails userDetails = createPrincipalDetails();
            CloverResponseDto responseDto = new CloverResponseDto(0);

            given(cloverService.getCloverCount(eq(TEST_MEMBER_ID)))
                    .willReturn(responseDto);

            // When
            ResponseHandler<CloverResponseDto> result = cloverController.getCloverCount(userDetails);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getCloverCount()).isEqualTo(0);
            verify(cloverService).getCloverCount(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("성공 - 클로버 개수가 많은 경우")
        void getCloverCount_LargeCount_Success() {
            // Given
            PrincipalDetails userDetails = createPrincipalDetails();
            int largeCount = 9999;
            CloverResponseDto responseDto = new CloverResponseDto(largeCount);

            given(cloverService.getCloverCount(eq(TEST_MEMBER_ID)))
                    .willReturn(responseDto);

            // When
            ResponseHandler<CloverResponseDto> result = cloverController.getCloverCount(userDetails);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData().getCloverCount()).isEqualTo(largeCount);
            verify(cloverService).getCloverCount(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 사용자를 찾을 수 없음")
        void getCloverCount_UserNotFound_ThrowsException() {
            // Given
            PrincipalDetails userDetails = createPrincipalDetails();

            given(cloverService.getCloverCount(eq(TEST_MEMBER_ID)))
                    .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> cloverController.getCloverCount(userDetails))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verify(cloverService).getCloverCount(TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회원 ID")
        void getCloverCount_InvalidMemberId_ThrowsException() {
            // Given
            Long invalidMemberId = 999L;
            PrincipalDetails userDetails = new PrincipalDetails(
                    invalidMemberId,
                    TEST_OAUTH_ID,
                    "USER",
                    TEST_NICKNAME,
                    "test@example.com"
            );

            given(cloverService.getCloverCount(eq(invalidMemberId)))
                    .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

            // When & Then
            assertThatThrownBy(() -> cloverController.getCloverCount(userDetails))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verify(cloverService).getCloverCount(invalidMemberId);
        }
    }

    private PrincipalDetails createPrincipalDetails() {
        return new PrincipalDetails(
                TEST_MEMBER_ID,
                TEST_OAUTH_ID,
                "USER",
                TEST_NICKNAME,
                "test@example.com"
        );
    }
}