package com.example.live_backend.domain.auth.dto;

import lombok.Getter;

@Getter
public class LoginRequestDto {
	private String email;
	private String oauthId;
	private String nickname;
}