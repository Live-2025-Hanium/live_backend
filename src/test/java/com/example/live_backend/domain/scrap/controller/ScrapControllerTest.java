package com.example.live_backend.domain.scrap.controller;

import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.domain.scrap.dto.request.ScrapCursorRequestDto;
import com.example.live_backend.domain.scrap.dto.request.ScrapDeleteRequestDto;
import com.example.live_backend.domain.scrap.dto.response.ScrapToggleResponseDto;
import com.example.live_backend.domain.scrap.dto.response.ScrapMessage;
import com.example.live_backend.domain.scrap.service.ScrapService;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.page.CursorTemplate;
import com.example.live_backend.global.security.PrincipalDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("스크랩 컨트롤러 단위 테스트")
class ScrapControllerTest {

    @Mock
    private ScrapService scrapService;

    @Mock
    private PrincipalDetails principalDetails;

    @InjectMocks
    private ScrapController scrapController;

    private final Long TEST_MEMBER_ID = 1L;
    private final Long TEST_BOARD_ID = 1L;

    @BeforeEach
    void setUp() {
        when(principalDetails.getMemberId()).thenReturn(TEST_MEMBER_ID);
    }

    @Nested
    @DisplayName("POST /api/v1/scraps/boards/{boardId}/toggle")
    class ToggleScrapTest {

        @Test
        @DisplayName("스크랩 추가 - 성공")
        void toggleScrap_AddScrap_Success() {
            // given
            given(scrapService.toggleScrap(TEST_MEMBER_ID, TEST_BOARD_ID)).willReturn(true);

            // when
            ResponseHandler<ScrapToggleResponseDto> response = 
                    scrapController.toggleScrap(principalDetails, TEST_BOARD_ID);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().isScraped()).isTrue();
            assertThat(response.getData().getMessage()).isEqualTo(ScrapMessage.SCRAP_ADDED.getMessage());
            verify(scrapService).toggleScrap(TEST_MEMBER_ID, TEST_BOARD_ID);
        }

        @Test
        @DisplayName("스크랩 취소 - 성공")
        void toggleScrap_RemoveScrap_Success() {
            // given
            given(scrapService.toggleScrap(TEST_MEMBER_ID, TEST_BOARD_ID)).willReturn(false);

            // when
            ResponseHandler<ScrapToggleResponseDto> response = 
                    scrapController.toggleScrap(principalDetails, TEST_BOARD_ID);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().isScraped()).isFalse();
            assertThat(response.getData().getMessage()).isEqualTo(ScrapMessage.SCRAP_REMOVED.getMessage());
            verify(scrapService).toggleScrap(TEST_MEMBER_ID, TEST_BOARD_ID);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 - 예외 발생")
        void toggleScrap_BoardNotFound_ThrowException() {
            // given
            Long invalidBoardId = 999L;
            given(scrapService.toggleScrap(TEST_MEMBER_ID, invalidBoardId))
                    .willThrow(new CustomException(ErrorCode.BOARD_NOT_FOUND));

            // when & then
            assertThrows(CustomException.class, () -> 
                    scrapController.toggleScrap(principalDetails, invalidBoardId));
            
            verify(scrapService).toggleScrap(TEST_MEMBER_ID, invalidBoardId);
        }

        @Test
        @DisplayName("존재하지 않는 회원 - 예외 발생")
        void toggleScrap_MemberNotFound_ThrowException() {
            // given
            given(scrapService.toggleScrap(TEST_MEMBER_ID, TEST_BOARD_ID))
                    .willThrow(new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            // when & then
            assertThrows(CustomException.class, () -> 
                    scrapController.toggleScrap(principalDetails, TEST_BOARD_ID));
            
            verify(scrapService).toggleScrap(TEST_MEMBER_ID, TEST_BOARD_ID);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/scraps/boards")
    class RemoveScrapsTest {

        @Test
        @DisplayName("다중 스크랩 삭제 - 성공")
        void removeScraps_Success() {
            // given
            ScrapDeleteRequestDto requestDto = new ScrapDeleteRequestDto();
            List<Long> boardIds = Arrays.asList(1L, 2L, 3L);
            ReflectionTestUtils.setField(requestDto, "boardIds", boardIds);

            // when
            ResponseHandler<Void> response = scrapController.removeScraps(principalDetails, requestDto);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isNull();
            verify(scrapService).removeScraps(eq(TEST_MEMBER_ID), any(ScrapDeleteRequestDto.class));
        }

        @Test
        @DisplayName("빈 게시글 ID 목록 - 예외 발생")
        void removeScraps_EmptyBoardIds_ThrowException() {
            // given
            ScrapDeleteRequestDto requestDto = new ScrapDeleteRequestDto();
            ReflectionTestUtils.setField(requestDto, "boardIds", Collections.emptyList());

            doThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE))
                    .when(scrapService).removeScraps(eq(TEST_MEMBER_ID), any(ScrapDeleteRequestDto.class));

            // when & then
            assertThrows(CustomException.class, () -> 
                    scrapController.removeScraps(principalDetails, requestDto));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/scraps")
    class GetScrapListTest {

        @Test
        @DisplayName("스크랩 목록 조회 - 성공")
        void getScrapList_Success() {
            // given
            ScrapCursorRequestDto requestDto = new ScrapCursorRequestDto();
            requestDto.setCursorId(null);
            requestDto.setSize(20);
            
            CursorTemplate<Long, BoardListResponseDto> mockResponse = 
                    CursorTemplate.of(Collections.emptyList());
            given(scrapService.getScrapList(TEST_MEMBER_ID, null, 20)).willReturn(mockResponse);

            // when
            ResponseHandler<CursorTemplate<Long, BoardListResponseDto>> response = 
                    scrapController.getScrapList(principalDetails, requestDto);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().hasNext()).isFalse();
            assertThat(response.getData().content()).isEmpty();
            verify(scrapService).getScrapList(TEST_MEMBER_ID, null, 20);
        }

        @Test
        @DisplayName("커서 ID로 다음 페이지 조회 - 성공")
        void getScrapList_WithCursor_Success() {
            // given
            ScrapCursorRequestDto requestDto = new ScrapCursorRequestDto();
            requestDto.setCursorId(10L);
            requestDto.setSize(20);
            
            CursorTemplate<Long, BoardListResponseDto> mockResponse = 
                    CursorTemplate.ofWithNextCursor(5L, Collections.emptyList());
            given(scrapService.getScrapList(TEST_MEMBER_ID, 10L, 20)).willReturn(mockResponse);

            // when
            ResponseHandler<CursorTemplate<Long, BoardListResponseDto>> response = 
                    scrapController.getScrapList(principalDetails, requestDto);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().hasNext()).isTrue();
            assertThat(response.getData().nextCursor()).isEqualTo(5L);
            verify(scrapService).getScrapList(TEST_MEMBER_ID, 10L, 20);
        }

        @Test
        @DisplayName("회원을 찾을 수 없음 - 예외 발생")
        void getScrapList_MemberNotFound_ThrowException() {
            // given
            ScrapCursorRequestDto requestDto = new ScrapCursorRequestDto();
            requestDto.setSize(20);
            
            given(scrapService.getScrapList(TEST_MEMBER_ID, null, 20))
                    .willThrow(new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            // when & then
            assertThrows(CustomException.class, () -> 
                    scrapController.getScrapList(principalDetails, requestDto));
            
            verify(scrapService).getScrapList(TEST_MEMBER_ID, null, 20);
        }
    }
}