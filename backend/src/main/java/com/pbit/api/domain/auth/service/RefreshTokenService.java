package com.pbit.api.domain.auth.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbit.api.domain.auth.entity.ClientType;
import com.pbit.api.domain.auth.entity.RefreshToken;
import com.pbit.api.domain.auth.repository.RefreshTokenRepository;
import com.pbit.api.global.auth.JwtProperties;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

// Refresh Token 발급·재발급(rotation)·폐기. 엔드포인트(/auth/refresh, /auth/logout)는 별도 이슈에서 연결한다
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private static final int TOKEN_BYTES = 32; // 256비트 난수
	private static final SecureRandom RANDOM = new SecureRandom();

	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtProperties jwtProperties;
	private final Clock clock;

	@Transactional
	public IssuedRefreshToken issue(UUID userId, ClientType clientType) {
		String rawToken = generateRawToken();
		Instant now = clock.instant();
		Instant expiresAt = now.plus(jwtProperties.refreshTokenTtl());
		refreshTokenRepository.save(new RefreshToken(userId, RefreshTokenHasher.hash(rawToken), clientType, expiresAt, now));
		return new IssuedRefreshToken(userId, clientType, rawToken, expiresAt);
	}

	// 기존 토큰을 폐기하고 같은 사용자·클라이언트로 새 토큰을 발급한다.
	// 없음·만료·이미 폐기 → REFRESH_TOKEN_INVALID
	@Transactional
	public IssuedRefreshToken rotate(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
		}
		String tokenHash = RefreshTokenHasher.hash(rawToken);
		RefreshToken current = refreshTokenRepository.findByTokenHash(tokenHash)
				.orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));
		if (refreshTokenRepository.revokeIfActive(tokenHash, clock.instant()) == 0) {
			throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
		}
		return issue(current.getUserId(), current.getClientType());
	}

	// 로그아웃. 없거나 이미 폐기된 토큰이어도 예외 없이 끝낸다
	@Transactional
	public void revoke(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			return;
		}
		refreshTokenRepository.revokeIfActive(RefreshTokenHasher.hash(rawToken), clock.instant());
	}

	private static String generateRawToken() {
		byte[] bytes = new byte[TOKEN_BYTES];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

}
