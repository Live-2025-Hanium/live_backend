package com.example.live_backend.domain.auth.controller.docs;

import com.example.live_backend.domain.auth.dto.response.LoginResponseDto;
import com.example.live_backend.global.error.response.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Demo Authentication", description = "공모전 심사용 테스트 로그인 API")
public interface DemoAuthControllerDocs {

    @Operation(
        summary = "테스트 로그인",
        description = "공모전 심사용 테스트 계정으로 자동 로그인합니다. " +
                     "별도의 인증 과정 없이 미리 생성된 테스트 계정으로 JWT 토큰이 발급됩니다. " +
                     "프론트엔드에서 버튼 클릭 한 번으로 즉시 로그인이 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "테스트 로그인 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponseDto.class),
                examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "data": {
                            "id": 1,
                            "oauthId": "DEMO_USER_001",
                            "email": "demo@live-contest.com",
                            "nickname": "심사위원",
                            "profileImageUrl": null,
                            "role": "USER",
                            "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                            "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                            "isNewUser": false
                        }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "테스트 계정을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "error": {
                            "code": "USER_NOT_FOUND",
                            "message": "사용자를 찾을 수 없습니다."
                        }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "error": {
                            "code": "INTERNAL_SERVER_ERROR",
                            "message": "서버 내부 오류가 발생했습니다."
                        }
                    }
                    """)
            )
        )
    })
    ResponseHandler<LoginResponseDto> demoLogin();
}