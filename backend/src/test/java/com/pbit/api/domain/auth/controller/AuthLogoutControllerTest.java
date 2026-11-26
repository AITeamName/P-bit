package com.pbit.api.domain.auth.controller;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.pbit.api.domain.auth.service.LoginService;
import com.pbit.api.domain.auth.service.RefreshTokenService;
import com.pbit.api.domain.auth.service.SignupService;
import com.pbit.api.domain.auth.service.TokenRefreshService;
import com.pbit.api.global.config.CorsConfig;
import com.pbit.api.global.config.SecurityConfig;
import com.pbit.api.global.error.ErrorResponseWriter;
import com.pbit.api.support.TestCookieConfig;
import com.pbit.api.support.TestJwtConfig;

import jakarta.servlet.http.Cookie;

// POST /auth/logout 웹 계층: 쿠키의 토큰을 폐기하는지, 쿠키를 지우는지, 인증 없이 호출되는지. 서비스는 목이다.
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, CorsConfig.class, ErrorResponseWriter.class, TestJwtConfig.class, TestCookieConfig.class})
class AuthLogoutControllerTest {

	private static final String LOGOUT_URL = "/api/v1/auth/logout";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RefreshTokenService refreshTokenService;

	// AuthController 생성에 필요. 이 테스트에서는 쓰지 않는다
	@MockitoBean
	private SignupService signupService;

	@MockitoBean
	private LoginService loginService;

	@MockitoBean
	private TokenRefreshService tokenRefreshService;

	@Test
	void 쿠키의_토큰을_폐기하고_204와_쿠키_삭제를_응답한다() throws Exception {
		mockMvc.perform(post(LOGOUT_URL).cookie(new Cookie("refresh_token", "my-refresh")))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""))
			.andExpect(header().string(HttpHeaders.SET_COOKIE, allOf(
				containsString("refresh_token="), containsString("Path=/api/v1/auth"),
				containsString("Max-Age=0"), containsString("HttpOnly"))));
		verify(refreshTokenService).revoke("my-refresh");
	}

	@Test
	void 쿠키가_없어도_204와_쿠키_삭제를_응답한다() throws Exception {
		mockMvc.perform(post(LOGOUT_URL))
			.andExpect(status().isNoContent())
			.andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
		verify(refreshTokenService).revoke(isNull());
	}

	@Test
	void 만료된_Access_Token이_함께_와도_로그아웃할_수_있다() throws Exception {
		String expired = TestJwtConfig.providerAt(TestJwtConfig.NOW.minus(Duration.ofHours(1)))
			.issueAccessToken(UUID.randomUUID(), "USER");

		mockMvc.perform(post(LOGOUT_URL).cookie(new Cookie("refresh_token", "my-refresh"))
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
			.andExpect(status().isNoContent());
		verify(refreshTokenService).revoke("my-refresh");
	}

}
