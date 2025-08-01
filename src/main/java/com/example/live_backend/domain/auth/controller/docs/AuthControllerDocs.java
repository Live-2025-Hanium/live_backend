package com.example.live_backend.domain.auth.controller.docs;

import com.example.live_backend.domain.auth.dto.request.KakaoLoginRequestDto;
import com.example.live_backend.domain.auth.dto.request.LogoutRequestDto;
import com.example.live_backend.domain.auth.dto.request.RefreshRequestDto;
import com.example.live_backend.domain.auth.dto.response.LoginResponseDto;
import com.example.live_backend.domain.auth.dto.response.TokensResponseDto;
import com.example.live_backend.global.error.response.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "인증 및 토큰 관리 API")
public interface AuthControllerDocs {

    @Operation(
        summary = "카카오 소셜 로그인",
        description = "카카오 OAuth2 인증을 통한 로그인 또는 회원가입을 처리합니다. " +
                     "신규 사용자인 경우 자동으로 회원가입이 됩니다. " +
                     "로그인 성공 시 JWT 토큰이 발급되어 온보딩 과정에서 사용됩니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "카카오 로그인 요청 정보",
            required = true,
            content = @Content(
                schema = @Schema(implementation = KakaoLoginRequestDto.class),
                examples = @ExampleObject(value = """
                    {
                        "oauthId": "kakao_123456789",
                        "email": "user@example.com",
                        "nickname": "카카오닉네임",
                        "profileImageUrl": "https://k.kakaocdn.net/profile/sample.jpg"
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
                        "data": {
                            "id": 1,
                            "oauthId": "kakao_123456789",
                            "email": "user@example.com",
                            "nickname": "카카오닉네임",
                            "profileImageUrl": "https://k.kakaocdn.net/profile/sample.jpg",
                            "role": "USER",
                            "isNewUser": true
                        }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "error": {
                            "code": "INVALID_INPUT",
                            "message": "잘못된 입력값입니다."
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
    ResponseHandler<LoginResponseDto> kakaoLogin(@RequestBody KakaoLoginRequestDto request);

    @Operation(
        summary = "액세스 토큰 갱신",
        description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다. " +
                     "만료된 액세스 토큰으로 API 호출 시 401 응답을 받으면 이 API를 호출하세요.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "토큰 갱신 요청 정보",
            required = true,
            content = @Content(
                schema = @Schema(implementation = RefreshRequestDto.class),
                examples = @ExampleObject(value = """
                    {
                        "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcklkIjo..."
                    }
                    """)
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "토큰 갱신 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokensResponseDto.class),
                examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "data": {
                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.NEW_ACCESS_TOKEN...",
                            "refreshToken": "eyJhbGciOiJIUzI1NiJ9.NEW_REFRESH_TOKEN...",
                            "accessTokenExpiresAt": "2024-01-01T12:00:00",
                            "refreshTokenExpiresAt": "2024-01-08T12:00:00"
                        }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 리프레시 토큰",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "error": {
                            "code": "INVALID_TOKEN",
                            "message": "유효하지 않은 토큰입니다."
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
    ResponseHandler<TokensResponseDto> refresh(@RequestBody RefreshRequestDto request);

    @Operation(
        summary = "로그아웃",
        description = "리프레시 토큰을 무효화하여 로그아웃을 처리합니다. " +
                     "클라이언트에서는 로컬에 저장된 Access/Refresh Token을 제거해야 합니다. " +
                     "만료된 토큰으로도 로그아웃이 가능합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "로그아웃 요청 정보",
            required = true,
            content = @Content(
                schema = @Schema(implementation = LogoutRequestDto.class),
                examples = @ExampleObject(value = """
                    {
                        "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcklkIjo..."
                    }
                    """)
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "data": null
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 리프레시 토큰",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "error": {
                            "code": "INVALID_TOKEN",
                            "message": "유효하지 않은 토큰입니다."
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
    ResponseHandler<Void> logout(@RequestBody LogoutRequestDto request);
} 