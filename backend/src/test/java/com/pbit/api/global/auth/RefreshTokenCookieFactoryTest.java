package com.pbit.api.global.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import com.pbit.api.support.TestJwtConfig;

class RefreshTokenCookieFactoryTest {

	private RefreshTokenCookieFactory factory(boolean secure) {
		return new RefreshTokenCookieFactory(TestJwtConfig.PROPERTIES, new RefreshCookieProperties(secure));
	}

	@Test
	void 발급_쿠키는_HttpOnly_SameSite_Lax_14일이다() {
		ResponseCookie cookie = factory(true).create("raw-token");

		assertThat(cookie.getName()).isEqualTo("refresh_token");
		assertThat(cookie.getValue()).isEqualTo("raw-token");
		assertThat(cookie.isHttpOnly()).isTrue();
		assertThat(cookie.isSecure()).isTrue();
		assertThat(cookie.getSameSite()).isEqualTo("Lax");
		assertThat(cookie.getPath()).isEqualTo("/api/v1/auth");
		assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(14));
		assertThat(cookie.toString())
				.contains("refresh_token=raw-token", "Path=/api/v1/auth", "Max-Age=1209600", "Secure", "HttpOnly", "SameSite=Lax");
	}

	@Test
	void 로컬_설정이면_Secure가_빠진다() {
		ResponseCookie cookie = factory(false).create("raw-token");

		assertThat(cookie.isSecure()).isFalse();
		assertThat(cookie.toString()).doesNotContain("Secure").contains("HttpOnly", "SameSite=Lax");
	}

	@Test
	void 삭제_쿠키는_같은_이름_경로에_Max_Age_0이다() {
		ResponseCookie cookie = factory(true).delete();

		assertThat(cookie.getName()).isEqualTo("refresh_token");
		assertThat(cookie.getValue()).isEmpty();
		assertThat(cookie.getPath()).isEqualTo("/api/v1/auth");
		assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
		assertThat(cookie.isHttpOnly()).isTrue();
		assertThat(cookie.toString()).contains("Max-Age=0");
	}

}
