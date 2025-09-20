package com.example.live_backend.domain.auth.v2.controller.docs;

import com.example.live_backend.domain.auth.v2.dto.request.OAuthCallbackRequest;
import com.example.live_backend.domain.auth.dto.response.LoginResponseDto;
import com.example.live_backend.global.error.response.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication V2", description = "웹 OAuth 인증 API (V2)")
public interface AuthControllerV2Docs {

    @Operation(
        summary = "카카오 OAuth 콜백 처리",
        description = "웹에서 카카오 OAuth 인증 후 인가 코드를 받아 로그인/회원가입을 처리합니다. " +
                     "인가 코드를 카카오에 전송하여 액세스 토큰을 받고, " +
                     "사용자 정보를 조회한 뒤 JWT 토큰을 발급합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "OAuth 콜백 요청 정보",
            required = true,
            content = @Content(
                schema = @Schema(implementation = OAuthCallbackRequest.class),
                examples = @ExampleObject(value = """
                    {
                        "code": "AUTHORIZATION_CODE_FROM_KAKAO",
                        "redirectUri": "http://localhost:3000/auth/callback"
                    }
                    """)
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponseDto.class),
                examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "code": "SUCCESS",
                        "message": "성공",
                        "data": {
                            "user": {
                                "id": 1,
                                "email": "user@example.com",
                                "nickname": "사용자닉네임",
                                "profileImageUrl": "https://k.kakaocdn.net/profile/sample.jpg",
                                "role": "USER"
                            },
                            "isNewUser": false,
                            "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                            "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
                        }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (인가 코드 누락 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "code": "INVALID_INPUT",
                        "message": "잘못된 입력값입니다.",
                        "data": null
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "502",
            description = "카카오 인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "code": "KAKAO_AUTH_FAILED",
                        "message": "카카오 인증 처리에 실패했습니다.",
                        "data": null
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
                        "code": "INTERNAL_SERVER_ERROR",
                        "message": "서버 내부 오류가 발생했습니다.",
                        "data": null
                    }
                    """)
            )
        )
    })
    ResponseHandler<LoginResponseDto> kakaoOAuthCallback(@Valid @RequestBody OAuthCallbackRequest request);
}