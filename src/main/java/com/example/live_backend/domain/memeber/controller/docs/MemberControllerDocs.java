package com.example.live_backend.domain.memeber.controller.docs;

import com.example.live_backend.domain.memeber.dto.MemberProfileRequestDto;
import com.example.live_backend.domain.memeber.dto.MemberResponseDto;
import com.example.live_backend.domain.memeber.dto.NicknameCheckResponseDto;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Member", description = "회원 프로필 관리 API")
public interface MemberControllerDocs {

    @Operation(
        summary = "닉네임 중복 확인",
        description = "닉네임 사용 가능 여부를 확인합니다. 자신의 현재 닉네임은 제외하고 확인됩니다. " +
                     "온보딩 과정과 마이페이지 수정 시 모두 사용됩니다. " +
                     "닉네임 정책: 2-20자, 한글/영문/숫자만 허용, 공백/특수문자/이모지 금지, 대소문자 구분"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "닉네임 중복 확인 완료",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NicknameCheckResponseDto.class),
                examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "data": {
                            "available": true,
                            "message": "사용 가능한 닉네임입니다."
                        }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (닉네임 형식 오류 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "error": {
                            "code": "MEMBER_NICKNAME_CHARACTER_INVALID",
                            "message": "닉네임은 한글, 영문, 숫자만 사용 가능합니다."
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
                        "success": false,
                        "error": {
                            "code": "UNAUTHORIZED",
                            "message": "인증이 필요합니다."
                        }
                    }
                    """)
            )
        )
    })
    ResponseHandler<NicknameCheckResponseDto> checkNickname(
        @Parameter(
            description = "확인할 닉네임 (2-20자, 한글/영문/숫자만)",
            required = true,
            example = "새로운닉네임123"
        )
        @RequestParam String nickname,
        @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(
        summary = "회원 프로필 등록/수정",
        description = "인증된 사용자의 프로필 정보를 등록하거나 갱신합니다. " +
                     "온보딩 완료와 마이페이지 수정에서 모두 사용됩니다. " +
                     "프로필 이미지는 S3 presigned URL을 통해 업로드한 후 accessUrl을 사용하세요.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "프로필 등록/수정 정보",
            required = true,
            content = @Content(
                schema = @Schema(implementation = MemberProfileRequestDto.class),
                examples = @ExampleObject(value = """
                    {
                        "nickname": "새로운닉네임123",
                        "profileImageUrl": "https://s3.amazonaws.com/profile/image.jpg",
                        "gender": "MALE",
                        "birthYear": 1995,
                        "birthMonth": 3,
                        "birthDay": 15,
                        "occupation": "STUDENT",
                        "occupationDetail": null
                    }
                    """)
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "프로필 등록/수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MemberResponseDto.class),
                examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "data": {
                            "id": 1,
                            "nickname": "새로운닉네임123",
                            "profileImageUrl": "https://s3.amazonaws.com/profile/image.jpg",
                            "gender": "MALE",
                            "birthDate": "1995-03-15",
                            "occupation": "STUDENT",
                            "occupationDetail": null
                        }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검사 실패 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "error": {
                            "code": "VALIDATION_FAILED",
                            "message": "입력값 검증에 실패했습니다."
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
                        "success": false,
                        "error": {
                            "code": "UNAUTHORIZED",
                            "message": "인증이 필요합니다."
                        }
                    }
                    """)
            )
        )
    })
    ResponseHandler<MemberResponseDto> registerProfile(
        @Valid @RequestBody MemberProfileRequestDto dto,
        @AuthenticationPrincipal PrincipalDetails userDetails
    );

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다. 마지막 설문조사 일시도 포함됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    ResponseHandler<MemberResponseDto> getMyProfile(
        @AuthenticationPrincipal PrincipalDetails userDetails
    );
} 