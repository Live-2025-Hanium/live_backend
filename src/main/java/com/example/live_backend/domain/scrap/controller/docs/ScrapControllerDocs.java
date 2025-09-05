package com.example.live_backend.domain.scrap.controller.docs;

import com.example.live_backend.domain.board.dto.response.BoardListResponseDto;
import com.example.live_backend.domain.scrap.dto.request.ScrapCursorRequestDto;
import com.example.live_backend.domain.scrap.dto.request.ScrapDeleteRequestDto;
import com.example.live_backend.domain.scrap.dto.response.ScrapDeleteResponseDto;
import com.example.live_backend.domain.scrap.dto.response.ScrapToggleResponseDto;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.page.CursorTemplate;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "스크랩", description = "게시글 스크랩 관리 API")
public interface ScrapControllerDocs {

    @Operation(
        summary = "게시글 스크랩 토글",
        description = """
            게시글의 스크랩 상태를 토글합니다.
            - 스크랩이 되어있지 않은 경우: 스크랩 추가
            - 이미 스크랩된 경우: 스크랩 취소
            
            좋아요 기능과 동일한 토글 방식으로 동작합니다.
            """,
        parameters = {
            @Parameter(
                name = "boardId",
                description = "스크랩할 게시글 ID",
                required = true,
                example = "1"
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "스크랩 토글 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseHandler.class),
                examples = {
                    @ExampleObject(
                        name = "스크랩 추가",
                        value = """
                            {
                                "timestamp": "2024-01-01T12:00:00",
                                "success": true,
                                "message": "SUCCESS",
                                "data": {
                                    "isScraped": true,
                                    "message": "스크랩이 추가되었습니다."
                                },
                                "error": null
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "스크랩 취소",
                        value = """
                            {
                                "timestamp": "2024-01-01T12:00:00",
                                "success": true,
                                "message": "SUCCESS",
                                "data": {
                                    "isScraped": false,
                                    "message": "스크랩이 취소되었습니다."
                                },
                                "error": null
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-01-01T12:00:00",
                        "success": false,
                        "message": "요청 처리 중 오류가 발생했습니다.",
                        "data": null,
                        "error": {
                            "code": "DENIED_UNAUTHORIZED_USER",
                            "message": "로그인되지 않은 유저의 접근입니다."
                        }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "게시글을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-01-01T12:00:00",
                        "success": false,
                        "message": "요청 처리 중 오류가 발생했습니다.",
                        "data": null,
                        "error": {
                            "code": "BOARD_NOT_FOUND",
                            "message": "존재하지 않는 게시글입니다."
                        }
                    }
                    """)
            )
        )
    })
    ResponseHandler<ScrapToggleResponseDto> toggleScrap(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @PathVariable Long boardId
    );

    @Operation(
        summary = "게시글 다중 스크랩 취소",
        description = """
            여러 게시글의 스크랩을 한번에 취소합니다.
            스크랩 목록 편집 모드에서 선택한 게시글들을 일괄 삭제할 때 사용됩니다.
            최대 100개까지 한번에 삭제 가능합니다.
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "삭제할 게시글 ID 목록",
            required = true,
            content = @Content(
                schema = @Schema(implementation = ScrapDeleteRequestDto.class),
                examples = @ExampleObject(value = """
                    {
                        "boardIds": [1, 2, 3, 4, 5]
                    }
                    """)
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "다중 스크랩 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-01-01T12:00:00",
                        "success": true,
                        "message": "SUCCESS",
                        "data": {
                            "requestedCount": 5,
                            "deletedCount": 5
                        },
                        "error": null
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (빈 배열, 100개 초과 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-01-01T12:00:00",
                        "success": false,
                        "message": "요청 처리 중 오류가 발생했습니다.",
                        "data": null,
                        "error": {
                            "code": "INVALID_INPUT_VALUE",
                            "message": "잘못된 입력값입니다."
                        }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-01-01T12:00:00",
                        "success": false,
                        "message": "요청 처리 중 오류가 발생했습니다.",
                        "data": null,
                        "error": {
                            "code": "DENIED_UNAUTHORIZED_USER",
                            "message": "로그인되지 않은 유저의 접근입니다."
                        }
                    }
                    """)
            )
        )
    })
    ResponseHandler<ScrapDeleteResponseDto> removeScraps(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @RequestBody @Valid ScrapDeleteRequestDto requestDto
    );

    @Operation(
        summary = "스크랩 목록 조회",
        description = """
            사용자가 스크랩한 게시글 목록을 조회합니다.
            커서 기반 페이징을 사용하여 무한 스크롤을 지원합니다.
            최신 스크랩순으로 정렬됩니다.
            """,
        parameters = {
            @Parameter(
                name = "cursorId",
                description = "커서 ID (다음 페이지 조회시 사용)",
                required = false,
                example = "10"
            ),
            @Parameter(
                name = "size",
                description = "한 페이지당 조회할 개수 (1-100, 기본값: 20)",
                required = false,
                example = "20"
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "스크랩 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseHandler.class),
                examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-01-01T12:00:00",
                        "success": true,
                        "message": "SUCCESS",
                        "data": {
                            "hasNext": true,
                            "nextCursor": 45,
                            "content": [
                                {
                                    "id": 50,
                                    "title": "최신 스크랩한 게시글",
                                    "category": {
                                        "id": 1,
                                        "name": "공지사항"
                                    },
                                    "relatedOrganization": "서울시",
                                    "thumbnailImageUrl": "https://example.com/image1.jpg",
                                    "authorNickname": "관리자",
                                    "viewCount": 150,
                                    "totalReactionCount": 0,
                                    "createdAt": "2024-01-01T10:00:00"
                                },
                                {
                                    "id": 48,
                                    "title": "두번째 스크랩 게시글",
                                    "category": {
                                        "id": 2,
                                        "name": "정책뉴스"
                                    },
                                    "relatedOrganization": "보건복지부",
                                    "thumbnailImageUrl": null,
                                    "authorNickname": "정책담당자",
                                    "viewCount": 85,
                                    "totalReactionCount": 0,
                                    "createdAt": "2023-12-31T15:30:00"
                                }
                            ]
                        },
                        "error": null
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-01-01T12:00:00",
                        "success": false,
                        "message": "요청 처리 중 오류가 발생했습니다.",
                        "data": null,
                        "error": {
                            "code": "DENIED_UNAUTHORIZED_USER",
                            "message": "로그인되지 않은 유저의 접근입니다."
                        }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "timestamp": "2024-01-01T12:00:00",
                        "success": false,
                        "message": "요청 처리 중 오류가 발생했습니다.",
                        "data": null,
                        "error": {
                            "code": "MEMBER_NOT_FOUND",
                            "message": "존재하지 않는 회원입니다."
                        }
                    }
                    """)
            )
        )
    })
    ResponseHandler<CursorTemplate<Long, BoardListResponseDto>> getScrapList(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @ModelAttribute @Valid ScrapCursorRequestDto requestDto
    );
}