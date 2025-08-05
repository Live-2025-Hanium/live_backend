package com.example.live_backend.domain.memeber.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.live_backend.domain.memeber.controller.docs.MemberControllerDocs;
import com.example.live_backend.domain.memeber.dto.MemberProfileRequestDto;
import com.example.live_backend.domain.memeber.dto.MemberResponseDto;
import com.example.live_backend.domain.memeber.dto.NicknameCheckResponseDto;
import com.example.live_backend.domain.memeber.service.MemberService;
import com.example.live_backend.global.error.response.ResponseHandler;
import com.example.live_backend.global.security.PrincipalDetails;
import com.example.live_backend.global.security.annotation.AuthenticatedApi;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/members", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

	private final MemberService memberService;

	//온보딩 (정보입력 플로우)
	// 1. 카카오 로그인 시 JWT 토큰 이미 발급됨
	// 2. 닉네임 중복 확인 (JWT 토큰 필요)
	// 3. 프로필 이미지 업로드 (JWT 토큰 필요)  
	// 4. 프로필 등록으로 온보딩 완료 (JWT 토큰 필요)

	@Override
	@AuthenticatedApi(reason = "로그인된 사용자만 닉네임 중복 확인 가능")
	@GetMapping("/nickname/check")
	public ResponseHandler<NicknameCheckResponseDto> checkNickname(
		@RequestParam String nickname,
		@AuthenticationPrincipal PrincipalDetails userDetails
	) {
		Long currentUserId = userDetails.getMemberId();
		NicknameCheckResponseDto response = memberService.checkNicknameAvailability(nickname, currentUserId);
		return ResponseHandler.success(response);
	}

	@Override
	@AuthenticatedApi(reason = "프로필 등록/수정은 로그인된 사용자만 가능")
	@PostMapping("/profile")
	public ResponseHandler<MemberResponseDto> registerProfile(
		@Valid @RequestBody MemberProfileRequestDto dto,
		@AuthenticationPrincipal PrincipalDetails userDetails
	) {
		Long userId = userDetails.getMemberId();
		MemberResponseDto response = memberService.registerOrUpdateProfile(dto, userId);
		return ResponseHandler.success(response);
	}

	@Override
	@AuthenticatedApi(reason = "로그인된 사용자만 자신의 정보 조회 가능")
	@GetMapping("/me")
	public ResponseHandler<MemberResponseDto> getMyProfile(
		@AuthenticationPrincipal PrincipalDetails userDetails
	) {
		Long userId = userDetails.getMemberId();
		MemberResponseDto response = memberService.getMemberById(userId);
		return ResponseHandler.success(response);
	}
}