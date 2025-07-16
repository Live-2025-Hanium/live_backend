package com.example.live_backend.domain.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

	private final JwtParser jwtParser;
	private final SecretKey secretKey;

	public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
		this.jwtParser = Jwts.parserBuilder()
			.setSigningKey(this.secretKey)
			.build();
	}

	public String createJwt(String category, Long userId, String oauthId, String role, long ttlMs) {
		Date now = new Date();
		return Jwts.builder()
			.claim("category", category)
			.claim("userId", userId)
			.claim("oauthId", oauthId)
			.claim("role", role)
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + ttlMs))
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
	}

	private Claims getClaims(String token) throws JwtException {
		return jwtParser.parseClaimsJws(token).getBody();
	}

	public boolean isExpired(String token) {
		try {
			Date exp = getClaims(token).getExpiration();
			return exp.before(new Date());
		} catch (ExpiredJwtException e) {
			return true;
		}
	}

	public String getCategory(String token) {
		return getClaims(token).get("category", String.class);
	}

	public Long getUserId(String token) {
		return getClaims(token).get("userId", Long.class);
	}

	public String getOauthId(String token) {
		return getClaims(token).get("oauthId", String.class);
	}

	public String getRole(String token) {
		return getClaims(token).get("role", String.class);
	}
}