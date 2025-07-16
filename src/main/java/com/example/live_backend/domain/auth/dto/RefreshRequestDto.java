package com.example.live_backend.domain.auth.dto;

import lombok.Getter;

@Getter
public class RefreshRequestDto {
	private String refreshToken;
}