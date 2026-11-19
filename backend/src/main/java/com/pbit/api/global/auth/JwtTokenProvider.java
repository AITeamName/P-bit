package com.pbit.api.global.auth;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

// Access Token(JWT, HS256) 발급·검증
@Component
public class JwtTokenProvider {

	private static final String ROLE_CLAIM = "role";

	private final SecretKey key;
	private final JwtProperties properties;
	private final Clock clock;
	private final JwtParser parser;

	public JwtTokenProvider(JwtProperties properties, Clock clock) {
		this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
		this.properties = properties;
		this.clock = clock;
		this.parser = Jwts.parser()
				.verifyWith(key)
				.clock(() -> Date.from(clock.instant()))
				.build();
	}

	public String issueAccessToken(UUID userId, String role) {
		Instant now = clock.instant();
		return Jwts.builder()
				.subject(userId.toString())
				.claim(ROLE_CLAIM, role)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(properties.accessTokenTtl())))
				.signWith(key, Jwts.SIG.HS256)
				.compact();
	}

	// 응답의 expiresIn(초)
	public long accessTokenExpiresInSeconds() {
		return properties.accessTokenTtl().toSeconds();
	}

	// 만료 → TOKEN_EXPIRED, 서명 불일치·형식 오류·필수 값 누락 → TOKEN_INVALID
	public AuthUser parse(String token) {
		try {
			Claims claims = parser.parseSignedClaims(token).getPayload();
			String role = claims.get(ROLE_CLAIM, String.class);
			if (claims.getSubject() == null || role == null) {
				throw new BusinessException(ErrorCode.TOKEN_INVALID);
			}
			return new AuthUser(UUID.fromString(claims.getSubject()), role);
		} catch (ExpiredJwtException e) {
			throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
		} catch (JwtException | IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.TOKEN_INVALID);
		}
	}

}
