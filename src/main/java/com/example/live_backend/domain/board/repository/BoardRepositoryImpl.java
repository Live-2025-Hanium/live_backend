package com.example.live_backend.domain.board.repository;

import com.example.live_backend.domain.board.entity.Board;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.live_backend.domain.board.entity.QBoard.board;


@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<Board> searchBoardsWithCursor(String keyword, Long cursor, int size) {
        return queryFactory
                .selectFrom(board)
                .where(
                        board.isDeleted.eq(false),
                        searchKeyword(keyword),
                        cursorCondition(cursor)
                )
                .orderBy(board.id.desc())
                .limit(size + 1)
                .fetch();
    }

    private BooleanExpression searchKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        
        String trimmedKeyword = keyword.trim().toLowerCase();
        
        return board.title.lower().contains(trimmedKeyword)
                .or(board.content.lower().contains(trimmedKeyword));
    }


    private BooleanExpression cursorCondition(Long cursor) {
        if (cursor == null) {
            return null;
        }
        
        return board.id.lt(cursor);
    }
    
    
} 