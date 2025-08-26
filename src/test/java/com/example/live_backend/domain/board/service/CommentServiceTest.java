package com.example.live_backend.domain.board.service;

import com.example.live_backend.domain.board.dto.request.CommentCreateRequestDto;
import com.example.live_backend.domain.board.dto.request.CommentUpdateRequestDto;
import com.example.live_backend.domain.board.dto.response.CommentResponseDto;
import com.example.live_backend.domain.board.entity.Board;
import com.example.live_backend.domain.board.entity.Category;
import com.example.live_backend.domain.board.entity.Comment;
import com.example.live_backend.domain.board.entity.CommentLike;
import com.example.live_backend.domain.board.repository.BoardRepository;
import com.example.live_backend.domain.board.repository.CommentLikeRepository;
import com.example.live_backend.domain.board.repository.CommentRepository;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.entity.vo.Profile;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 테스트")
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;


    @Mock
    private BoardRepository boardRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CommentLikeService commentLikeService;

    private Board board;
    private Member author;
    private Comment parentComment;
    private Comment reply;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트 데이터 설정
        author = Member.builder()
                .oauthId("123")
                .email("test@test.com")
                .role(com.example.live_backend.domain.memeber.Role.USER)
                .profile(Profile.builder()
                        .nickname("테스트유저")
                        .build())
                .build();
        
        // Member ID 설정
        setId(author, 1L);

        Category category = Category.builder()
                .name("테스트카테고리")
                .build();
        
        // Category ID 설정
        setId(category, 1L);

        board = Board.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .category(category)
                .author(author)
                .build();
        
        // Board ID 설정
        setId(board, 1L);

        parentComment = Comment.builder()
                .content("부모 댓글")
                .board(board)
                .author(author)
                .parentComment(null)
                .build();
        
        // Comment ID 설정
        setId(parentComment, 1L);

        reply = Comment.builder()
                .content("대댓글")
                .board(board)
                .author(author)
                .parentComment(parentComment)
                .build();
        
        // Reply ID 설정
        setId(reply, 2L);
    }

    // Reflection을 사용하여 ID 필드 설정하는 헬퍼 메소드
    private void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    @Test
    @DisplayName("댓글 목록 조회 - 빈 목록 반환")
    void getCommentsByBoardId_EmptyList() {
        // given
        Long boardId = 1L;
        Long memberId = 1L;
        
        // 부모 댓글이 없는 경우
        given(commentRepository.findParentCommentsByBoardId(boardId)).willReturn(List.of());

        // when
        List<CommentResponseDto> result = commentService.getCommentsByBoardId(boardId, memberId);

        // then
        assertThat(result).isEmpty();
        then(commentRepository).should().findParentCommentsByBoardId(boardId);
        then(commentLikeService).shouldHaveNoInteractions();
    }
    
    @Test
    @DisplayName("댓글 목록 조회 - 댓글이 있는 경우")
    void getCommentsByBoardId_WithComments() {
        // given
        Long boardId = 1L;
        Long memberId = 1L;
        
        given(commentRepository.findParentCommentsByBoardId(boardId)).willReturn(List.of(parentComment));
        given(commentRepository.findRepliesByParentCommentIds(List.of(1L))).willReturn(List.of(reply));
        
        CommentLikeService.CommentLikeMetadata metadata = new CommentLikeService.CommentLikeMetadata(
            Map.of(1L, 5L, 2L, 3L),
            Set.of(1L)
        );
        given(commentLikeService.getCommentLikeMetadata(anyList(), eq(memberId))).willReturn(metadata);

        // when
        List<CommentResponseDto> result = commentService.getCommentsByBoardId(boardId, memberId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("부모 댓글");
        assertThat(result.get(0).getReplies()).hasSize(1);
        assertThat(result.get(0).getReplies().get(0).getContent()).isEqualTo("대댓글");
        
        then(commentRepository).should().findParentCommentsByBoardId(boardId);
        then(commentRepository).should().findRepliesByParentCommentIds(List.of(1L));
        then(commentLikeService).should().getCommentLikeMetadata(anyList(), eq(memberId));
    }

    @Test
    @DisplayName("댓글 작성 - 성공")
    void createComment_Success() {
        // given
        Long boardId = 1L;
        Long authorId = 1L;
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto("새로운 댓글");

        given(boardRepository.findByIdAndNotDeleted(boardId)).willReturn(Optional.of(board));
        given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            return comment;
        });

        // when
        Long result = commentService.createComment(boardId, requestDto, authorId);

        // then
        then(commentRepository).should().save(argThat(comment -> 
                comment.getContent().equals("새로운 댓글") &&
                comment.getBoard().equals(board) &&
                comment.getAuthor().equals(author) &&
                comment.getParentComment() == null
        ));
    }

    @Test
    @DisplayName("댓글 작성 - 게시글이 존재하지 않는 경우")
    void createComment_BoardNotFound() {
        // given
        Long boardId = 999L;
        Long authorId = 1L;
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto("새로운 댓글");

        given(boardRepository.findByIdAndNotDeleted(boardId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(boardId, requestDto, authorId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOARD_NOT_FOUND);
    }

    @Test
    @DisplayName("대댓글 작성 - 성공")
    void createReply_Success() {
        // given
        Long boardId = 1L;
        Long parentCommentId = 1L;
        Long authorId = 1L;
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto("새로운 대댓글");

        given(boardRepository.findByIdAndNotDeleted(boardId)).willReturn(Optional.of(board));
        given(commentRepository.findByIdAndNotDeleted(parentCommentId)).willReturn(Optional.of(parentComment));
        given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            return comment;
        });

        // when
        Long result = commentService.createReply(boardId, parentCommentId, requestDto, authorId);

        // then
        then(commentRepository).should().save(argThat(comment -> 
                comment.getContent().equals("새로운 대댓글") &&
                comment.getBoard().equals(board) &&
                comment.getAuthor().equals(author) &&
                comment.getParentComment().equals(parentComment)
        ));
    }

    @Test
    @DisplayName("대댓글 작성 - 대댓글의 대댓글은 불가능")
    void createReply_ReplyToReplyNotAllowed() {
        // given
        Long boardId = 1L;
        Long replyCommentId = 2L;
        Long authorId = 1L;
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto("대댓글의 대댓글");

        given(boardRepository.findByIdAndNotDeleted(boardId)).willReturn(Optional.of(board));
        given(commentRepository.findByIdAndNotDeleted(replyCommentId)).willReturn(Optional.of(reply));

        // when & then
        assertThatThrownBy(() -> commentService.createReply(boardId, replyCommentId, requestDto, authorId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("댓글 수정 - 성공")
    void updateComment_Success() {
        // given
        Long commentId = 1L;
        Long memberId = 1L;
        CommentUpdateRequestDto requestDto = new CommentUpdateRequestDto("수정된 댓글");

        given(commentRepository.findByIdAndNotDeleted(commentId)).willReturn(Optional.of(parentComment));

        // when
        commentService.updateComment(commentId, requestDto, memberId);

        // then
        assertThat(parentComment.getContent()).isEqualTo("수정된 댓글");
    }

    @Test
    @DisplayName("댓글 수정 - 작성자가 아닌 경우 실패")
    void updateComment_NotAuthor() {
        // given
        Long commentId = 1L;
        Long memberId = 999L; // 다른 사용자
        CommentUpdateRequestDto requestDto = new CommentUpdateRequestDto("수정된 댓글");

        given(commentRepository.findByIdAndNotDeleted(commentId)).willReturn(Optional.of(parentComment));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(commentId, requestDto, memberId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_FORBIDDEN);
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    void deleteComment_Success() {
        // given
        Long commentId = 1L;
        Long memberId = 1L;

        given(commentRepository.findByIdAndNotDeleted(commentId)).willReturn(Optional.of(parentComment));

        // when
        commentService.deleteComment(commentId, memberId);

        // then
        assertThat(parentComment.getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("댓글 좋아요 토글 - 좋아요 추가")
    void toggleCommentLike_AddLike() {
        // given
        Long commentId = 1L;
        Long memberId = 1L;

        given(commentRepository.findByIdAndNotDeleted(commentId)).willReturn(Optional.of(parentComment));

        // when
        commentService.toggleCommentLike(commentId, memberId);

        // then
        then(commentLikeService).should().toggleLike(parentComment, memberId);
    }

    @Test
    @DisplayName("댓글 좋아요 토글 - 좋아요 취소")
    void toggleCommentLike_RemoveLike() {
        // given
        Long commentId = 1L;
        Long memberId = 1L;
        CommentLike existingLike = CommentLike.builder()
                .comment(parentComment)
                .member(author)
                .build();

        given(commentRepository.findByIdAndNotDeleted(commentId)).willReturn(Optional.of(parentComment));

        // when
        commentService.toggleCommentLike(commentId, memberId);

        // then
        then(commentLikeService).should().toggleLike(parentComment, memberId);
    }
} 