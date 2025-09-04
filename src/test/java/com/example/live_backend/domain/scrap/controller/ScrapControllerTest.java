package com.example.live_backend.domain.scrap.controller;

import com.example.live_backend.config.WithMockPrincipalDetails;
import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.domain.scrap.dto.request.ScrapCursorRequestDto;
import com.example.live_backend.domain.scrap.dto.request.ScrapDeleteRequestDto;
import com.example.live_backend.domain.scrap.dto.response.ScrapToggleResponseDto;
import com.example.live_backend.domain.scrap.service.ScrapService;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.page.CursorTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScrapController.class)
@DisplayName("스크랩 컨트롤러 테스트")
class ScrapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScrapService scrapService;

    @Nested
    @DisplayName("POST /api/v1/scraps/boards/{boardId}/toggle")
    class ToggleScrapTest {

        @Test
        @WithMockPrincipalDetails(memberId = 1L)
        @DisplayName("스크랩 추가 - 성공")
        void toggleScrap_AddScrap_Success() throws Exception {
            // given
            Long boardId = 1L;
            given(scrapService.toggleScrap(1L, boardId)).willReturn(true);

            // when & then
            mockMvc.perform(post("/api/v1/scraps/boards/{boardId}/toggle", boardId)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isScraped").value(true))
                    .andExpect(jsonPath("$.data.message").value("스크랩이 추가되었습니다."));

            verify(scrapService).toggleScrap(1L, boardId);
        }

        @Test
        @WithMockPrincipalDetails(memberId = 1L)
        @DisplayName("스크랩 취소 - 성공")
        void toggleScrap_RemoveScrap_Success() throws Exception {
            // given
            Long boardId = 1L;
            given(scrapService.toggleScrap(1L, boardId)).willReturn(false);

            // when & then
            mockMvc.perform(post("/api/v1/scraps/boards/{boardId}/toggle", boardId)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isScraped").value(false))
                    .andExpect(jsonPath("$.data.message").value("스크랩이 취소되었습니다."));

            verify(scrapService).toggleScrap(1L, boardId);
        }

        @Test
        @DisplayName("인증되지 않은 사용자 - 401 에러")
        void toggleScrap_Unauthorized_Fail() throws Exception {
            // given
            Long boardId = 1L;

            // when & then
            mockMvc.perform(post("/api/v1/scraps/boards/{boardId}/toggle", boardId)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(scrapService, never()).toggleScrap(anyLong(), anyLong());
        }

        @Test
        @WithMockPrincipalDetails(memberId = 1L)
        @DisplayName("존재하지 않는 게시글 - 404 에러")
        void toggleScrap_BoardNotFound_Fail() throws Exception {
            // given
            Long boardId = 999L;
            given(scrapService.toggleScrap(1L, boardId))
                    .willThrow(new CustomException(ErrorCode.BOARD_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/api/v1/scraps/boards/{boardId}/toggle", boardId)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("BOARD_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/scraps/boards")
    class RemoveScrapsTest {

        @Test
        @WithMockPrincipalDetails(memberId = 1L)
        @DisplayName("다중 스크랩 삭제 - 성공")
        void removeScraps_Success() throws Exception {
            // given
            ScrapDeleteRequestDto requestDto = new ScrapDeleteRequestDto();
            List<Long> boardIds = Arrays.asList(1L, 2L, 3L);
            requestDto.getClass().getDeclaredField("boardIds").setAccessible(true);
            requestDto.getClass().getDeclaredField("boardIds").set(requestDto, boardIds);

            doNothing().when(scrapService).removeScraps(eq(1L), any(ScrapDeleteRequestDto.class));

            // when & then
            mockMvc.perform(delete("/api/v1/scraps/boards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(scrapService).removeScraps(eq(1L), any(ScrapDeleteRequestDto.class));
        }

        @Test
        @WithMockPrincipalDetails(memberId = 1L)
        @DisplayName("빈 배열로 삭제 요청 - 400 에러")
        void removeScraps_EmptyArray_Fail() throws Exception {
            // given
            String requestBody = "{\"boardIds\": []}";

            // when & then
            mockMvc.perform(delete("/api/v1/scraps/boards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockPrincipalDetails(memberId = 1L)
        @DisplayName("100개 초과 삭제 요청 - 400 에러")
        void removeScraps_ExceedLimit_Fail() throws Exception {
            // given
            List<Long> boardIds = new java.util.ArrayList<>();
            for (long i = 1; i <= 101; i++) {
                boardIds.add(i);
            }
            String requestBody = String.format("{\"boardIds\": %s}", 
                    objectMapper.writeValueAsString(boardIds));

            // when & then
            mockMvc.perform(delete("/api/v1/scraps/boards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("인증되지 않은 사용자 - 401 에러")
        void removeScraps_Unauthorized_Fail() throws Exception {
            // given
            ScrapDeleteRequestDto requestDto = new ScrapDeleteRequestDto();
            List<Long> boardIds = Arrays.asList(1L, 2L, 3L);
            requestDto.getClass().getDeclaredField("boardIds").setAccessible(true);
            requestDto.getClass().getDeclaredField("boardIds").set(requestDto, boardIds);

            // when & then
            mockMvc.perform(delete("/api/v1/scraps/boards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(scrapService, never()).removeScraps(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/scraps")
    class GetScrapListTest {

        @Test
        @WithMockPrincipalDetails(memberId = 1L)
        @DisplayName("스크랩 목록 조회 - 성공")
        void getScrapList_Success() throws Exception {
            // given
            CursorTemplate<Long, BoardListResponseDto> mockResponse = 
                    CursorTemplate.of(Collections.emptyList());
            given(scrapService.getScrapList(1L, null, 20)).willReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/scraps")
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    .andExpect(jsonPath("$.data.content").isArray());

            verify(scrapService).getScrapList(1L, null, 20);
        }

        @Test
        @WithMockPrincipalDetails(memberId = 1L)
        @DisplayName("커서 ID로 다음 페이지 조회 - 성공")
        void getScrapList_WithCursor_Success() throws Exception {
            // given
            Long cursorId = 10L;
            CursorTemplate<Long, BoardListResponseDto> mockResponse = 
                    CursorTemplate.ofWithNextCursor(5L, Collections.emptyList());
            given(scrapService.getScrapList(1L, cursorId, 20)).willReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/scraps")
                            .param("cursorId", String.valueOf(cursorId))
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.hasNext").value(true))
                    .andExpect(jsonPath("$.data.nextCursor").value(5));

            verify(scrapService).getScrapList(1L, cursorId, 20);
        }

        @Test
        @WithMockPrincipalDetails(memberId = 1L)
        @DisplayName("잘못된 size 파라미터 - 400 에러")
        void getScrapList_InvalidSize_Fail() throws Exception {
            // when & then
            mockMvc.perform(get("/api/v1/scraps")
                            .param("size", "0"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/scraps")
                            .param("size", "101"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(scrapService, never()).getScrapList(anyLong(), any(), anyInt());
        }

        @Test
        @DisplayName("인증되지 않은 사용자 - 401 에러")
        void getScrapList_Unauthorized_Fail() throws Exception {
            // when & then
            mockMvc.perform(get("/api/v1/scraps"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());

            verify(scrapService, never()).getScrapList(anyLong(), any(), anyInt());
        }

        @Test
        @WithMockPrincipalDetails(memberId = 1L)
        @DisplayName("회원을 찾을 수 없음 - 404 에러")
        void getScrapList_MemberNotFound_Fail() throws Exception {
            // given
            given(scrapService.getScrapList(1L, null, 20))
                    .willThrow(new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/v1/scraps")
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("MEMBER_NOT_FOUND"));
        }
    }
}