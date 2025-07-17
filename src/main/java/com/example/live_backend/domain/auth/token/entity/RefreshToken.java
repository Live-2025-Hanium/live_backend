package com.example.live_backend.domain.auth.token.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class RefreshToken {

	@Id
	private Long userId;

	private String token;

	public RefreshToken(Long userId, String token) {
		this.userId = userId;
		this.token = token;
	}

	public void updateToken(String token) {
		this.token = token;
	}
}