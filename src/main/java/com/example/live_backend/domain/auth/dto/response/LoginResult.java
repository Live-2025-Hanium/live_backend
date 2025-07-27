package com.example.live_backend.domain.auth.dto.response;

import com.example.live_backend.domain.auth.dto.AuthToken;

import lombok.Getter;

@Getter
public class LoginResult {
    private final LoginResponseDto response;
    private final AuthToken tokens;

    public LoginResult(LoginResponseDto response, AuthToken tokens) {
        this.response = response;
        this.tokens = tokens;
    }
} 