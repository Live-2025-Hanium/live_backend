package com.example.live_backend.domain.scrap.service;

import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.domain.board.entity.Board;
import com.example.live_backend.domain.board.entity.Category;
import com.example.live_backend.domain.board.repository.BoardRepository;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.scrap.dto.request.ScrapDeleteRequestDto;
import com.example.live_backend.domain.scrap.entity.Scrap;
import com.example.live_backend.domain.scrap.repository.ScrapRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import com.example.live_backend.global.page.CursorTemplate;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("스크랩 서비스 테스트")
class ScrapServiceTest {

    @InjectMocks
    private ScrapService scrapService;

    @Mock
    private ScrapRepository scrapRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private MemberRepository memberRepository;

    private Member member;
    private Board board;
    private Category category;
    private Scrap scrap;

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
        ReflectionTestUtils.setField(member, "id", 1L);

        category = Category.builder()
                .name("공지사항")
                .build();
        ReflectionTestUtils.setField(category, "id", 1L);

        board = Board.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .category(category)
                .author(member)
                .build();
        ReflectionTestUtils.setField(board, "id", 1L);

        scrap = Scrap.builder()
                .member(member)
                .board(board)
                .build();
        ReflectionTestUtils.setField(scrap, "id", 1L);
    }

    @Nested
    @DisplayName("스크랩 토글 기능")
    class ToggleScrapTest {

        @Test
        @DisplayName("스크랩이 없는 경우 - 스크랩 추가")
        void toggleScrap_WhenNotScraped_ShouldAddScrap() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));
            given(scrapRepository.findByMemberAndBoard(member, board)).willReturn(Optional.empty());
            given(scrapRepository.save(any(Scrap.class))).willReturn(scrap);

            // when
            boolean result = scrapService.toggleScrap(1L, 1L);

            // then
            assertThat(result).isTrue();
            verify(scrapRepository).save(any(Scrap.class));
            verify(scrapRepository, never()).delete(any(Scrap.class));
        }

        @Test
        @DisplayName("스크랩이 있는 경우 - 스크랩 취소")
        void toggleScrap_WhenAlreadyScraped_ShouldRemoveScrap() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));
            given(scrapRepository.findByMemberAndBoard(member, board)).willReturn(Optional.of(scrap));

            // when
            boolean result = scrapService.toggleScrap(1L, 1L);

            // then
            assertThat(result).isFalse();
            verify(scrapRepository).delete(scrap);
            verify(scrapRepository, never()).save(any(Scrap.class));
        }

        @Test
        @DisplayName("존재하지 않는 회원 - 예외 발생")
        void toggleScrap_WhenMemberNotFound_ShouldThrowException() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scrapService.toggleScrap(1L, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

            verify(boardRepository, never()).findById(anyLong());
            verify(scrapRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 게시글 - 예외 발생")
        void toggleScrap_WhenBoardNotFound_ShouldThrowException() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(boardRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scrapService.toggleScrap(1L, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOARD_NOT_FOUND);

            verify(scrapRepository, never()).save(any());
        }

        @Test
        @DisplayName("삭제된 게시글 - 예외 발생")
        void toggleScrap_WhenBoardIsDeleted_ShouldThrowException() {
            // given
            board.delete();
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(boardRepository.findById(1L)).willReturn(Optional.of(board));

            // when & then
            assertThatThrownBy(() -> scrapService.toggleScrap(1L, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOARD_NOT_FOUND);

            verify(scrapRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("다중 스크랩 삭제 기능")
    class RemoveScrapsTest {

        @Test
        @DisplayName("정상적으로 다중 스크랩 삭제")
        void removeScraps_Success() {
            // given
            ScrapDeleteRequestDto requestDto = new ScrapDeleteRequestDto();
            List<Long> boardIds = Arrays.asList(1L, 2L, 3L);
            ReflectionTestUtils.setField(requestDto, "boardIds", boardIds);

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when
            scrapService.removeScraps(1L, requestDto);

            // then
            verify(scrapRepository).deleteByMemberAndBoardIds(member, boardIds);
        }

        @Test
        @DisplayName("빈 게시글 ID 목록 - 예외 발생")
        void removeScraps_WhenEmptyBoardIds_ShouldThrowException() {
            // given
            ScrapDeleteRequestDto requestDto = new ScrapDeleteRequestDto();
            ReflectionTestUtils.setField(requestDto, "boardIds", List.of());

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> scrapService.removeScraps(1L, requestDto))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);

            verify(scrapRepository, never()).deleteByMemberAndBoardIds(any(), any());
        }

        @Test
        @DisplayName("null 게시글 ID 목록 - 예외 발생")
        void removeScraps_WhenNullBoardIds_ShouldThrowException() {
            // given
            ScrapDeleteRequestDto requestDto = new ScrapDeleteRequestDto();
            ReflectionTestUtils.setField(requestDto, "boardIds", null);

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> scrapService.removeScraps(1L, requestDto))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);

            verify(scrapRepository, never()).deleteByMemberAndBoardIds(any(), any());
        }
    }

    @Nested
    @DisplayName("스크랩 목록 조회 기능")
    class GetScrapListTest {

        @Test
        @DisplayName("정상적으로 스크랩 목록 조회")
        void getScrapList_Success() {
            // given
            Long cursorId = null;
            int size = 20;
            
            CursorTemplate<Long, BoardListResponseDto> mockResponse = CursorTemplate.of(List.of());
            
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(scrapRepository.findScrapsByMemberWithCursor(member, cursorId, size))
                    .willReturn(mockResponse);

            // when
            CursorTemplate<Long, BoardListResponseDto> result = 
                    scrapService.getScrapList(1L, cursorId, size);

            // then
            assertThat(result).isEqualTo(mockResponse);
            verify(scrapRepository).findScrapsByMemberWithCursor(member, cursorId, size);
        }

        @Test
        @DisplayName("커서 ID가 있는 경우 스크랩 목록 조회")
        void getScrapList_WithCursorId_Success() {
            // given
            Long cursorId = 10L;
            int size = 20;
            
            CursorTemplate<Long, BoardListResponseDto> mockResponse = 
                    CursorTemplate.ofWithNextCursor(5L, List.of());
            
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(scrapRepository.findScrapsByMemberWithCursor(member, cursorId, size))
                    .willReturn(mockResponse);

            // when
            CursorTemplate<Long, BoardListResponseDto> result = 
                    scrapService.getScrapList(1L, cursorId, size);

            // then
            assertThat(result).isEqualTo(mockResponse);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isEqualTo(5L);
        }

        @Test
        @DisplayName("존재하지 않는 회원 - 예외 발생")
        void getScrapList_WhenMemberNotFound_ShouldThrowException() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scrapService.getScrapList(1L, null, 20))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

            verify(scrapRepository, never()).findScrapsByMemberWithCursor(any(), any(), anyInt());
        }
    }
}