package com.pbit.api.domain.auth.service;

import java.util.UUID;

// 로그인 결과. refreshToken 원문은 컨트롤러에서 쿠키로만 내보내고 응답 본문(LoginResponse)에는 넣지 않는다
public record LoginResult(
	UUID userId,
	String accessToken,
	long expiresIn,
	boolean onboardingCompleted,
	String refreshToken
) {
}
