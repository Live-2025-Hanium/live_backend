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
			.claim(JwtConstants.CLAIM_CATEGORY, category)
			.claim(JwtConstants.CLAIM_USER_ID, userId)
			.claim(JwtConstants.CLAIM_OAUTH_ID, oauthId)
			.claim(JwtConstants.CLAIM_ROLE, role)
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + ttlMs))
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
	}

	private Claims getClaims(String token) throws JwtException {
		return jwtParser.parseClaimsJws(token).getBody();
	}

	public TokenInfo validateAndExtract(String token) {
		try {
			Claims claims = getClaims(token);
			Date expiration = claims.getExpiration();
			boolean isExpired = expiration.before(new Date());

			return TokenInfo.builder()
				.valid(!isExpired)
				.expired(isExpired)
				.category(claims.get(JwtConstants.CLAIM_CATEGORY, String.class))
				.userId(claims.get(JwtConstants.CLAIM_USER_ID, Long.class))
				.oauthId(claims.get(JwtConstants.CLAIM_OAUTH_ID, String.class))
				.role(claims.get(JwtConstants.CLAIM_ROLE, String.class))
				.build();
		} catch (ExpiredJwtException e) {

			Claims claims = e.getClaims();
			return TokenInfo.builder()
				.valid(false)
				.expired(true)
				.category(claims.get(JwtConstants.CLAIM_CATEGORY, String.class))
				.userId(claims.get(JwtConstants.CLAIM_USER_ID, Long.class))
				.oauthId(claims.get(JwtConstants.CLAIM_OAUTH_ID, String.class))
				.role(claims.get(JwtConstants.CLAIM_ROLE, String.class))
				.build();
		}
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
		return getClaims(token).get(JwtConstants.CLAIM_CATEGORY, String.class);
	}

	public Long getUserId(String token) {
		return getClaims(token).get(JwtConstants.CLAIM_USER_ID, Long.class);
	}

	public String getOauthId(String token) {
		return getClaims(token).get(JwtConstants.CLAIM_OAUTH_ID, String.class);
	}

	public String getRole(String token) {
		return getClaims(token).get(JwtConstants.CLAIM_ROLE, String.class);
	}
}