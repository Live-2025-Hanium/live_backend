package com.example.live_backend.domain.auth.token.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor
public class RefreshToken {

	@Id
	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "token", nullable = false, length = 500)
	private String token;

	public RefreshToken(Long userId, String token) {
		this.userId = userId;
		this.token = token;
	}

	public void updateToken(String token) {
		this.token = token;
	}
}