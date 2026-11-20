package com.pbit.api.domain.auth.service;

import java.util.UUID;

// 가입 결과. refreshToken 원문은 컨트롤러에서 쿠키로만 내보낸다
public record SignupResult(
	UUID userId,
	String accessToken,
	long expiresIn,
	boolean onboardingCompleted,
	String refreshToken
) {
}
