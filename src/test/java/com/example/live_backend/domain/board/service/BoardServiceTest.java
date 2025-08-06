package com.example.live_backend.domain.board.service;

import com.example.live_backend.domain.board.dto.request.BoardCreateRequestDto;
import com.example.live_backend.domain.board.dto.request.BoardUpdateRequestDto;
import com.example.live_backend.domain.board.dto.response.BoardDetailResponseDto;
import com.example.live_backend.domain.board.entity.Board;
import com.example.live_backend.domain.board.entity.BoardReaction;
import com.example.live_backend.domain.board.entity.Category;
import com.example.live_backend.domain.board.entity.enums.ReactionType;
import com.example.live_backend.domain.board.repository.*;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.global.error.exception.CustomException;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService 테스트")
class BoardServiceTest {

	@Mock private BoardRepository boardRepository;
	@Mock private BoardReactionRepository boardReactionRepository;
	@Mock private MemberRepository memberRepository;
	@Mock private CategoryRepository categoryRepository;
	@Mock private ImageRepository imageRepository;
	@Mock private BoardImageRepository boardImageRepository;

	@InjectMocks private BoardService boardService;

	private Member member;
	private Category category;
	private Board board;

	@BeforeEach
	void setUp() {
		member = mock(Member.class);
		category = mock(Category.class);
		board = createBoard(1L, "테스트 제목", "테스트 내용", category, member);
	}

	@Test @DisplayName("게시글 생성 성공")
	void createBoard_Success() {
		BoardCreateRequestDto dto = new BoardCreateRequestDto(
			"새 게시글", "새 내용", 1L, "한국고용정보원", null
		);
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
		when(boardRepository.save(any(Board.class))).thenReturn(board);

		Long id = boardService.createBoard(dto, 1L);

		assertThat(id).isEqualTo(1L);
		verify(boardRepository).save(any(Board.class));
	}

	@Test @DisplayName("존재하지 않는 작성자로 게시글 생성 실패")
	void createBoard_MemberNotFound() {
		BoardCreateRequestDto dto = new BoardCreateRequestDto(
			"새 게시글", "새 내용", 1L, "한국고용정보원", null
		);
		when(memberRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> boardService.createBoard(dto, 1L))
			.isInstanceOf(CustomException.class);
	}

	@Test @DisplayName("존재하지 않는 카테고리로 게시글 생성 실패")
	void createBoard_CategoryNotFound() {
		BoardCreateRequestDto dto = new BoardCreateRequestDto(
			"새 게시글", "새 내용", 999L, "한국고용정보원", null
		);
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> boardService.createBoard(dto, 1L))
			.isInstanceOf(CustomException.class);
	}

	@Test @DisplayName("게시글 수정 성공")
	void updateBoard_Success() {
		BoardUpdateRequestDto dto = new BoardUpdateRequestDto(
			"수정된 제목", "수정된 내용", 1L, "수정된 기관", null
		);
		when(boardRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(board));
		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

		boardService.updateBoard(1L, dto);

		verify(boardRepository).findByIdAndNotDeleted(1L);
		verify(categoryRepository).findById(1L);
	}

	@Test @DisplayName("존재하지 않는 게시글 수정 실패")
	void updateBoard_BoardNotFound() {
		BoardUpdateRequestDto dto = new BoardUpdateRequestDto(
			"수정된 제목", "수정된 내용", 1L, "수정된 기관", null
		);
		when(boardRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> boardService.updateBoard(1L, dto))
			.isInstanceOf(CustomException.class);
	}

	@Test @DisplayName("게시글 삭제 성공")
	void deleteBoard_Success() {
		when(boardRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(board));

		boardService.deleteBoard(1L);

		verify(boardRepository).findByIdAndNotDeleted(1L);
		assertThat(board.getIsDeleted()).isTrue();
	}

	@Test @DisplayName("게시글 상세 조회 성공 - 조회수 증가")
	void getBoardDetail_Success() {
		Long before = board.getViewCount();
		when(boardRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(board));
		when(boardReactionRepository.countReactionsByBoardId(1L)).thenReturn(List.of());
		when(boardReactionRepository.findActiveReactionsByBoardIdAndMemberId(1L, 1L))
			.thenReturn(List.of());

		BoardDetailResponseDto resp = boardService.getBoardDetail(1L, 1L);

		assertThat(resp.getId()).isEqualTo(1L);
		assertThat(board.getViewCount()).isEqualTo(before + 1);
	}

	@Test @DisplayName("새로운 반응 추가 성공")
	void toggleReaction_NewReaction_Success() {
		when(boardRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(board));
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(boardReactionRepository.findActiveReactionsByBoardIdAndMemberId(1L, 1L))
			.thenReturn(List.of());
		when(boardReactionRepository.findByBoardIdAndMemberIdAndReactionType(
			1L, 1L, ReactionType.EMPATHY))
			.thenReturn(Optional.empty());

		boardService.toggleReaction(1L, 1L, ReactionType.EMPATHY);

		verify(boardReactionRepository).save(any(BoardReaction.class));
	}

	@Test @DisplayName("같은 반응 다시 누르면 삭제")
	void toggleReaction_SameReaction_Delete() {
		BoardReaction existing = createBoardReaction(1L, board, member, ReactionType.EMPATHY);
		when(boardRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(board));
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(boardReactionRepository.findActiveReactionsByBoardIdAndMemberId(1L, 1L))
			.thenReturn(List.of(existing));

		boardService.toggleReaction(1L, 1L, ReactionType.EMPATHY);

		assertThat(existing.getDeletedAt()).isNotNull();
	}

	@Test @DisplayName("다른 반응으로 변경")
	void toggleReaction_DifferentReaction_Change() {
		BoardReaction existing = createBoardReaction(1L, board, member, ReactionType.EMPATHY);
		when(boardRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(board));
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(boardReactionRepository.findActiveReactionsByBoardIdAndMemberId(1L, 1L))
			.thenReturn(List.of(existing));
		when(boardReactionRepository.findByBoardIdAndMemberIdAndReactionType(
			1L, 1L, ReactionType.USEFUL))
			.thenReturn(Optional.empty());

		boardService.toggleReaction(1L, 1L, ReactionType.USEFUL);

		assertThat(existing.getDeletedAt()).isNotNull();
		verify(boardReactionRepository).save(any(BoardReaction.class));
	}

	@Test @DisplayName("이전에 같은 타입 반응이 있었다면 재활성화")
	void toggleReaction_ReactivateExisting() {
		BoardReaction existing = createBoardReaction(1L, board, member, ReactionType.USEFUL);
		existing.delete();
		when(boardRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(board));
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(boardReactionRepository.findActiveReactionsByBoardIdAndMemberId(1L, 1L))
			.thenReturn(List.of());
		when(boardReactionRepository.findByBoardIdAndMemberIdAndReactionType(
			1L, 1L, ReactionType.USEFUL))
			.thenReturn(Optional.of(existing));

		boardService.toggleReaction(1L, 1L, ReactionType.USEFUL);

		assertThat(existing.getDeletedAt()).isNull();
	}

	private Board createBoard(Long id, String title, String content,
		Category category, Member author) {
		Board b = Board.builder()
			.title(title)
			.content(content)
			.category(category)
			.relatedOrganization("테스트 기관")
			.author(author)
			.build();
		try {
			Field f = Board.class.getDeclaredField("id");
			f.setAccessible(true);
			f.set(b, id);
		} catch (Exception ignored) {}
		return b;
	}

	private BoardReaction createBoardReaction(Long id, Board board,
		Member member, ReactionType type) {
		BoardReaction r = BoardReaction.builder()
			.board(board)
			.member(member)
			.reactionType(type)
			.build();
		try {
			Field f = BoardReaction.class.getDeclaredField("id");
			f.setAccessible(true);
			f.set(r, id);
		} catch (Exception ignored) {}
		return r;
	}
}