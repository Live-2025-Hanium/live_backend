package com.example.live_backend.domain.scrap.service;

import com.example.live_backend.domain.board.entity.Board;
import com.example.live_backend.domain.board.repository.BoardRepository;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.memeber.repository.MemberRepository;
import com.example.live_backend.domain.scrap.dto.request.ScrapDeleteRequestDto;
import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.global.page.CursorTemplate;
import com.example.live_backend.domain.scrap.entity.Scrap;
import java.util.Optional;
import com.example.live_backend.domain.scrap.repository.ScrapRepository;
import com.example.live_backend.global.error.exception.CustomException;
import com.example.live_backend.global.error.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public boolean toggleScrap(Long memberId, Long boardId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        if (board.getIsDeleted()) {
            throw new CustomException(ErrorCode.BOARD_NOT_FOUND);
        }

        Optional<Scrap> existingScrap = scrapRepository.findByMemberAndBoard(member, board);
        
        if (existingScrap.isPresent()) {
            // 스크랩이 있으면 삭제 (토글 OFF)
            scrapRepository.delete(existingScrap.get());
            return false;
        } else {
            // 스크랩이 없으면 추가 (토글 ON)
            Scrap scrap = Scrap.builder()
                    .member(member)
                    .board(board)
                    .build();
            scrapRepository.save(scrap);
            return true;
        }
    }

    @Transactional
    public void removeScraps(Long memberId, ScrapDeleteRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Long> boardIds = requestDto.getBoardIds();
        if (boardIds == null || boardIds.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        scrapRepository.deleteByMemberAndBoardIds(member, boardIds);
    }

    public CursorTemplate<Long, BoardListResponseDto> getScrapList(Long memberId, Long cursorId, int size) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return scrapRepository.findScrapsByMemberWithCursor(member, cursorId, size);
    }


}