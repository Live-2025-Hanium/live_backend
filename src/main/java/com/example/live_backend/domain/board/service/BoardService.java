package com.example.live_backend.domain.board.service;

import com.example.live_backend.domain.board.dto.request.BoardCreateRequestDto;
import com.example.live_backend.domain.board.dto.request.BoardUpdateRequestDto;

import com.example.live_backend.domain.board.dto.response.BoardDetailResponseDto;
import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.domain.board.dto.response.BoardCategoryHomeResponseDto;
import com.example.live_backend.domain.board.dto.response.CategoryResponseDto;
import com.example.live_backend.global.page.CursorTemplate;
import com.example.live_backend.domain.board.entity.Board;
import com.example.live_backend.domain.board.entity.BoardReaction;
import com.example.live_backend.domain.board.entity.Category;
import com.example.live_backend.domain.board.entity.Image;
import com.example.live_backend.domain.board.entity.BoardImage;
import com.example.live_backend.domain.board.entity.enums.ReactionType;
import com.example.live_backend.domain.board.repository.BoardReactionRepository;
import com.example.live_backend.domain.board.repository.BoardRepository;
import com.example.live_backend.domain.board.repository.CategoryRepository;
import com.example.live_backend.domain.board.repository.ImageRepository;
import com.example.live_backend.domain.board.repository.BoardImageRepository;
import com.example.live_backend.domain.board.repository.CommentRepository;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardReactionRepository boardReactionRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final BoardImageRepository boardImageRepository;
    private final CommentRepository commentRepository;

    /**
     * 게시글 생성 (관리자만 가능)
     */
    @Transactional
    public Long createBoard(BoardCreateRequestDto requestDto, Long authorId) {
        Member author = memberRepository.findById(authorId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Board board = Board.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .category(category)
                .relatedOrganization(requestDto.getRelatedOrganization())
                .author(author)
                .build();

        Board savedBoard = boardRepository.save(board);

        if (requestDto.getImageUrls() != null && !requestDto.getImageUrls().isEmpty()) {
            processImages(savedBoard, requestDto.getImageUrls());
        }

        return savedBoard.getId();
    }

    /**
     * 게시글 수정 (관리자만 가능)
     */
    @Transactional
    public void updateBoard(Long boardId, BoardUpdateRequestDto requestDto) {
        Board board = findBoardById(boardId);
        
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        board.update(requestDto.getTitle(), requestDto.getContent(), category, requestDto.getRelatedOrganization());

        board.getBoardImages().clear();
        if (requestDto.getImageUrls() != null && !requestDto.getImageUrls().isEmpty()) {
            processImages(board, requestDto.getImageUrls());
        }
    }

    /**
     * 게시글 삭제 (관리자만 가능)
     */
    @Transactional
    public void deleteBoard(Long boardId) {
        Board board = findBoardById(boardId);
        board.delete();
    }

    /**
     * 게시글 상세 조회
     */
    @Transactional
    public BoardDetailResponseDto getBoardDetail(Long boardId, Long memberId) {
        Board board = findBoardById(boardId);

        board.incrementViewCount();

        Map<ReactionType, Long> reactionCounts = getReactionCounts(boardId);

        List<ReactionType> userReactions = getUserReactions(boardId, memberId);

        String authorNickname = getAuthorNickname(board.getAuthor());

        Long commentCount = commentRepository.countByBoardId(boardId);

        return new BoardDetailResponseDto(board, authorNickname, commentCount, reactionCounts, userReactions);
    }

    /**
     * 홈 화면용 카테고리별 게시글 조회 (각 카테고리당 최신 10개)
     */
    public List<BoardCategoryHomeResponseDto> getBoardsForHome(String sortBy) {
        List<Category> categories = categoryRepository.findAllOrderById();
        
        return categories.stream()
                .map(category -> {
                    List<Board> boards;
                    if ("views".equals(sortBy)) {
                        boards = boardRepository.findBoardsByCategoryOrderByViews(
                                category.getName(), 
                                org.springframework.data.domain.PageRequest.of(0, 10)
                        );
                    } else {
                        boards = boardRepository.findLatestBoardsByCategory(
                                category.getName(), 
                                org.springframework.data.domain.PageRequest.of(0, 10)
                        );
                    }
                    
                    List<Long> boardIds = boards.stream()
                            .map(Board::getId)
                            .toList();
                    
                    Map<Long, Long> totalReactionCounts = getTotalReactionCounts(boardIds);
                    
                    List<BoardListResponseDto> boardDtos = boards.stream()
                            .map(board -> {
                                String authorNickname = getAuthorNickname(board.getAuthor());
                                Long totalReactionCount = totalReactionCounts.getOrDefault(board.getId(), 0L);
                                return new BoardListResponseDto(board, authorNickname, totalReactionCount);
                            })
                            .toList();
                    
                    return new BoardCategoryHomeResponseDto(category.getName(), boardDtos);
                })
                .filter(categoryHome -> !categoryHome.getBoards().isEmpty())
                .toList();
    }

    /**
     * 커서 기반 카테고리별 게시글 조회 (무한 스크롤)
     */
    public CursorTemplate<Long, BoardListResponseDto> getBoardsByCategoryWithCursor(
            String category, Long cursor, Integer size) {
        
        int pageSize = size != null ? size : 20;
        List<Board> boards = boardRepository.findByCategoryWithCursor(
                category, 
                cursor, 
                org.springframework.data.domain.PageRequest.of(0, pageSize + 1)
        );
        
        return createCursorResponse(boards, pageSize);
    }

    /**
     * 커서 기반 키워드 검색 (무한 스크롤)
     */
    public CursorTemplate<Long, BoardListResponseDto> searchBoardsWithCursor(
            String keyword, Long cursor, Integer size, String sortBy) {
        
        int pageSize = size != null ? size : 20;
        List<Board> boards;
        
        if ("views".equals(sortBy)) {
            boards = boardRepository.searchBoardsWithCursorOrderByViews(keyword, cursor, cursor, pageSize);
        } else {
            boards = boardRepository.searchBoardsWithCursor(keyword, cursor, pageSize);
        }
        
        return createCursorResponse(boards, pageSize);
    }



    /**
     * 카테고리 목록 조회
     */
    public List<CategoryResponseDto> getCategories() {
        return categoryRepository.findAllOrderById()
                .stream()
                .map(CategoryResponseDto::new)
                .toList();
    }

    /**
     * 게시글 반응 토글
     */
    @Transactional
    public void toggleReaction(Long boardId, Long memberId, ReactionType reactionType) {
        Board board = findBoardById(boardId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 기존 활성 반응들 조회
        List<BoardReaction> existingReactions = 
                boardReactionRepository.findActiveReactionsByBoardIdAndMemberId(boardId, memberId);

        // 같은 타입의 반응이 이미 있는지 확인
        java.util.Optional<BoardReaction> sameTypeReaction = existingReactions.stream()
                .filter(reaction -> reaction.getReactionType() == reactionType)
                .findFirst();

        if (sameTypeReaction.isPresent()) {
            // 같은 반응이 활성화되어 있으면 삭제 (토글 off)
            sameTypeReaction.get().delete();
        } else {
            // 다른 타입의 활성 반응들을 모두 삭제
            existingReactions.forEach(BoardReaction::delete);

            // 새로운 반응 생성 또는 기존 반응 활성화
            java.util.Optional<BoardReaction> existingReaction = 
                    boardReactionRepository.findByBoardIdAndMemberIdAndReactionType(boardId, memberId, reactionType);

            if (existingReaction.isPresent()) {
                // 기존에 해당 타입의 반응이 있었다면 활성화
                existingReaction.get().activate();
            } else {
                // 새로운 반응 생성
                BoardReaction newReaction = BoardReaction.builder()
                        .board(board)
                        .member(member)
                        .reactionType(reactionType)
                        .build();
                boardReactionRepository.save(newReaction);
            }
        }
    }

    private void processImages(Board board, List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size(); i++) {
            String imageUrl = imageUrls.get(i);

            Image image = imageRepository.findByS3Url(imageUrl)
                    .orElseGet(() -> {
                        return imageRepository.save(Image.builder()
                                .s3Url(imageUrl)
                                .build());
                    });
            
            BoardImage boardImage = BoardImage.builder()
                    .board(board)
                    .image(image)
                    .build();
            
            boardImageRepository.save(boardImage);
        }
    }



    private Board findBoardById(Long boardId) {
        return boardRepository.findByIdAndNotDeleted(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Map<ReactionType, Long> getReactionCounts(Long boardId) {
        List<BoardReactionRepository.ReactionCount> reactionCounts = 
                boardReactionRepository.countReactionsByBoardId(boardId);

        Map<ReactionType, Long> countMap = new EnumMap<>(ReactionType.class);
        
        for (ReactionType type : ReactionType.values()) {
            countMap.put(type, 0L);
        }
        
        reactionCounts.forEach(rc -> countMap.put(rc.getReactionType(), rc.getCount()));
        
        return countMap;
    }

    private List<ReactionType> getUserReactions(Long boardId, Long memberId) {
        if (memberId == null) {
            return List.of();
        }

        List<BoardReaction> userReactions = 
                boardReactionRepository.findActiveReactionsByBoardIdAndMemberId(boardId, memberId);
        
        return userReactions.stream()
                .map(BoardReaction::getReactionType)
                .toList();
    }

    private Map<Long, Long> getTotalReactionCounts(List<Long> boardIds) {
        if (boardIds.isEmpty()) {
            return Map.of();
        }

        List<BoardReactionRepository.BoardReactionCount> boardReactionCounts = 
                boardReactionRepository.countReactionsByBoardIds(boardIds);

        return boardReactionCounts.stream()
                .collect(Collectors.groupingBy(
                        BoardReactionRepository.BoardReactionCount::getBoardId,
                        Collectors.summingLong(BoardReactionRepository.BoardReactionCount::getCount)
                ));
    }

    private CursorTemplate<Long, BoardListResponseDto> createCursorResponse(
            List<Board> boards, int pageSize) {
        
        boolean hasNext = boards.size() > pageSize;
        List<Board> content = hasNext ? boards.subList(0, pageSize) : boards;
        
        List<Long> boardIds = content.stream()
                .map(Board::getId)
                .toList();
        
        Map<Long, Long> totalReactionCounts = getTotalReactionCounts(boardIds);
        
        List<BoardListResponseDto> boardDtos = content.stream()
                .map(board -> {
                    String authorNickname = getAuthorNickname(board.getAuthor());
                    Long totalReactionCount = totalReactionCounts.getOrDefault(board.getId(), 0L);
                    return new BoardListResponseDto(board, authorNickname, totalReactionCount);
                })
                .toList();
        
        if (!hasNext) {
            return CursorTemplate.of(boardDtos);
        }
        
        Long nextCursor = content.get(content.size() - 1).getId();
        return CursorTemplate.ofWithNextCursor(nextCursor, boardDtos);
    }

    private String getAuthorNickname(Member author) {
        return author.getProfile() != null ? author.getProfile().getNickname() : "Unknown";
    }
} 