package com.example.live_backend.domain.memeber.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.live_backend.domain.memeber.dto.MemberProfileRequestDto;
import com.example.live_backend.domain.memeber.dto.MemberResponseDto;
import com.example.live_backend.domain.memeber.dto.NicknameCheckResponseDto;
import com.example.live_backend.domain.memeber.service.MemberService;
import com.example.live_backend.global.security.PrincipalDetails;

import jakarta.validation.Valid;

@Tag(name = "Member", description = "회원 프로필 관리 API")
@RestController
@RequestMapping(path = "/api/members", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/nickname/check")
	@Operation(
		summary = "닉네임 중복 확인",
		description = "닉네임 사용 가능 여부를 확인합니다. 로그인된 사용자의 경우 자신의 현재 닉네임은 제외하고 확인됩니다.",
		parameters = {
			@Parameter(
				name = "nickname",
				description = "확인할 닉네임 (2-20자)",
				required = true,
				example = "사용자닉네임"
			)
		},
		responses = {
			@ApiResponse(responseCode = "200", description = "닉네임 중복 확인 결과",
				content = @Content(
					schema = @Schema(implementation = NicknameCheckResponseDto.class)
				)
			),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 (닉네임 형식 오류 등)",
				content = @Content(schema = @Schema())
			)
		}
	)
	public ResponseEntity<NicknameCheckResponseDto> checkNickname(
		@RequestParam String nickname,
		@AuthenticationPrincipal PrincipalDetails userDetails
	) {
		Long currentUserId = userDetails.getMemberId();
		NicknameCheckResponseDto response = memberService.checkNicknameAvailability(nickname, currentUserId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/profile")
	@Operation(
		summary = "회원 프로필 등록/수정",
		description = "인증된 사용자의 프로필(닉네임, 프로필 이미지, 성별, 생년월일, 직업 등)을 등록하거나 갱신합니다.",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "프로필 등록/수정 정보",
			required = true,
			content = @Content(
				schema = @Schema(implementation = MemberProfileRequestDto.class)
			)
		),
		responses = {
			@ApiResponse(responseCode = "200", description = "프로필 등록/수정 성공",
				content = @Content(
					schema = @Schema(implementation = MemberResponseDto.class)
				)
			),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검사 실패 등)",
				content = @Content(schema = @Schema())
			)
		}
	)
	public ResponseEntity<MemberResponseDto> registerProfile(
		@Valid @RequestBody MemberProfileRequestDto dto,
		@AuthenticationPrincipal PrincipalDetails userDetails
	) {
		Long userId = userDetails.getMemberId();
		MemberResponseDto response = memberService.registerOrUpdateProfile(dto, userId);
		return ResponseEntity.ok(response);
	}
}