package com.pbit.api.domain.auth.service;

// 재발급 결과. refreshToken 원문은 컨트롤러에서 쿠키로만 내보낸다
public record TokenRefreshResult(
	String accessToken,
	long expiresIn,
	String refreshToken
) {
}
