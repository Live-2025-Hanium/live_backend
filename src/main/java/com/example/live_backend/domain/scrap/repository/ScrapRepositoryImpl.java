package com.example.live_backend.domain.scrap.repository;

import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.domain.memeber.entity.Member;
import com.example.live_backend.domain.scrap.entity.Scrap;
import com.example.live_backend.global.page.CursorTemplate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.live_backend.domain.scrap.entity.QScrap.scrap;
import static com.example.live_backend.domain.board.entity.QBoard.board;
import static com.example.live_backend.domain.memeber.entity.QMember.member;
import static com.example.live_backend.domain.board.entity.QCategory.category;

@Repository
@RequiredArgsConstructor
public class ScrapRepositoryImpl implements ScrapRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorTemplate<Long, BoardListResponseDto> findScrapsByMemberWithCursor(Member targetMember, Long cursorId, int size) {
        List<Scrap> scraps = queryFactory
                .selectFrom(scrap)
                .join(scrap.board, board).fetchJoin()
                .join(board.author, member).fetchJoin()
                .join(board.category, category).fetchJoin()
                .where(
                        scrap.member.eq(targetMember),
                        scrap.board.isDeleted.eq(false),
                        cursorCondition(cursorId)
                )
                .orderBy(scrap.id.desc())
                .limit(size + 1)
                .fetch();

        boolean hasNext = scraps.size() > size;
        if (hasNext) {
            scraps = scraps.subList(0, size);
        }

        List<BoardListResponseDto> boardDtos = scraps.stream()
                .map(s -> new BoardListResponseDto(
                        s.getBoard(), 
                        s.getBoard().getAuthor().getProfile().getNickname(),
                        null  // 스크랩 목록에서는 반응 수를 제공하지 않음
                ))
                .toList();

        if (hasNext && !scraps.isEmpty()) {
            Long nextCursor = scraps.get(scraps.size() - 1).getId();
            return CursorTemplate.ofWithNextCursor(nextCursor, boardDtos);
        } else {
            return CursorTemplate.of(boardDtos);
        }
    }

    private BooleanExpression cursorCondition(Long cursorId) {
        return cursorId != null ? scrap.id.lt(cursorId) : null;
    }
}