package com.example.live_backend.domain.board.service;

import com.example.live_backend.domain.board.dto.response.BoardDetailResponseDto;
import com.example.live_backend.domain.board.entity.Board;
import com.example.live_backend.domain.board.entity.Category;
import com.example.live_backend.domain.board.repository.BoardReactionRepository;
import com.example.live_backend.domain.board.repository.BoardRepository;
import com.example.live_backend.domain.board.repository.CategoryRepository;
import com.example.live_backend.domain.board.repository.ImageRepository;
import com.example.live_backend.domain.board.repository.BoardImageRepository;
import com.example.live_backend.domain.board.repository.CommentRepository;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService 댓글 수 포함 상세조회 테스트")
class BoardServiceCommentCountTest {

    @InjectMocks
    private BoardService boardService;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardReactionRepository boardReactionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private BoardImageRepository boardImageRepository;

    @Mock
    private CommentRepository commentRepository;

    private Board board;
    private Member author;

    @BeforeEach
    void setUp() {
        author = Member.builder()
                .oauthId("123")
                .email("test@test.com")
                .role(com.example.live_backend.domain.memeber.Role.USER)
                .profile(Profile.builder()
                        .nickname("테스트작성자")
                        .build())
                .build();

        Category category = Category.builder()
                .name("테스트카테고리")
                .build();

        board = Board.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .category(category)
                .author(author)
                .build();
    }

    @Test
    @DisplayName("게시글 상세조회 시 댓글 수가 포함되어야 한다")
    void getBoardDetail_ShouldIncludeCommentCount() {
        // given
        Long boardId = 1L;
        Long memberId = 1L;
        Long expectedCommentCount = 15L;

        given(boardRepository.findByIdAndNotDeleted(boardId)).willReturn(Optional.of(board));
        given(boardReactionRepository.countReactionsByBoardId(boardId)).willReturn(List.of());
        given(boardReactionRepository.findActiveReactionsByBoardIdAndMemberId(boardId, memberId)).willReturn(List.of());
        given(commentRepository.countByBoardId(boardId)).willReturn(expectedCommentCount);

        // when
        BoardDetailResponseDto result = boardService.getBoardDetail(boardId, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCommentCount()).isEqualTo(expectedCommentCount);
        assertThat(result.getTitle()).isEqualTo("테스트 게시글");
        assertThat(result.getContent()).isEqualTo("테스트 내용");
        assertThat(result.getAuthorNickname()).isEqualTo("테스트작성자");

        // CommentRepository의 countByBoardId가 호출되었는지 확인
        then(commentRepository).should().countByBoardId(boardId);
    }

    @Test
    @DisplayName("게시글 상세조회 시 댓글이 없으면 댓글 수는 0이어야 한다")
    void getBoardDetail_NoComments_ShouldReturnZeroCommentCount() {
        // given
        Long boardId = 1L;
        Long memberId = 1L;
        Long expectedCommentCount = 0L;

        given(boardRepository.findByIdAndNotDeleted(boardId)).willReturn(Optional.of(board));
        given(boardReactionRepository.countReactionsByBoardId(boardId)).willReturn(List.of());
        given(boardReactionRepository.findActiveReactionsByBoardIdAndMemberId(boardId, memberId)).willReturn(List.of());
        given(commentRepository.countByBoardId(boardId)).willReturn(expectedCommentCount);

        // when
        BoardDetailResponseDto result = boardService.getBoardDetail(boardId, memberId);

        // then
        assertThat(result.getCommentCount()).isEqualTo(0L);
        then(commentRepository).should().countByBoardId(boardId);
    }

    @Test
    @DisplayName("게시글 상세조회 시 조회수가 1 증가해야 한다")
    void getBoardDetail_ShouldIncrementViewCount() {
        // given
        Long boardId = 1L;
        Long memberId = 1L;
        Long initialViewCount = board.getViewCount();

        given(boardRepository.findByIdAndNotDeleted(boardId)).willReturn(Optional.of(board));
        given(boardReactionRepository.countReactionsByBoardId(boardId)).willReturn(List.of());
        given(boardReactionRepository.findActiveReactionsByBoardIdAndMemberId(boardId, memberId)).willReturn(List.of());
        given(commentRepository.countByBoardId(boardId)).willReturn(5L);

        // when
        BoardDetailResponseDto result = boardService.getBoardDetail(boardId, memberId);

        // then
        assertThat(board.getViewCount()).isEqualTo(initialViewCount + 1);
        assertThat(result.getViewCount()).isEqualTo(initialViewCount + 1);
    }

} 