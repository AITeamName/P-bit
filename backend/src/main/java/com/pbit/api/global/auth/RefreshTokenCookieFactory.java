package com.pbit.api.global.auth;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// 웹 클라이언트용 Refresh Token 쿠키. 응답에 Set-Cookie 헤더로 붙인다
// - HttpOnly: JS에서 읽지 못하게 (XSS로 탈취 방지)
// - SameSite=Lax: 다른 사이트에서 오는 POST에는 실리지 않게 (CSRF 완화)
// - Secure: https에서만 전송. 로컬(http)은 app.refresh-cookie.secure=false
// - Path=/api/v1/auth: 재발급·로그아웃 요청에만 실리게 범위를 좁힌다
@Component
@RequiredArgsConstructor
public class RefreshTokenCookieFactory {

	public static final String COOKIE_NAME = "refresh_token";
	static final String COOKIE_PATH = "/api/v1/auth";
	private static final String SAME_SITE = "Lax";

	private final JwtProperties jwtProperties;
	private final RefreshCookieProperties cookieProperties;

	public ResponseCookie create(String rawToken) {
		return base(rawToken).maxAge(jwtProperties.refreshTokenTtl()).build();
	}

	// 로그아웃 등에서 브라우저 쿠키를 지운다 (같은 이름·경로, Max-Age=0)
	public ResponseCookie delete() {
		return base("").maxAge(Duration.ZERO).build();
	}

	private ResponseCookie.ResponseCookieBuilder base(String value) {
		return ResponseCookie.from(COOKIE_NAME, value)
				.httpOnly(true)
				.secure(cookieProperties.secure())
				.sameSite(SAME_SITE)
				.path(COOKIE_PATH);
	}

}
