package com.pbit.api.domain.auth.controller;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import com.pbit.api.domain.auth.service.TokenRefreshResult;
import com.pbit.api.domain.auth.service.TokenRefreshService;
import com.pbit.api.global.config.CorsConfig;
import com.pbit.api.global.config.SecurityConfig;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;
import com.pbit.api.global.error.ErrorResponseWriter;
import com.pbit.api.support.TestCookieConfig;
import com.pbit.api.support.TestJwtConfig;

import jakarta.servlet.http.Cookie;

// POST /auth/refresh 웹 계층: 쿠키에서 토큰을 읽는지, 새 쿠키로 교체하는지, 인증 없이 호출되는지. 서비스는 목이다.
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, CorsConfig.class, ErrorResponseWriter.class, TestJwtConfig.class, TestCookieConfig.class})
class AuthRefreshControllerTest {

	private static final String REFRESH_URL = "/api/v1/auth/refresh";
	private static final Cookie OLD_COOKIE = new Cookie("refresh_token", "old-refresh");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TokenRefreshService tokenRefreshService;

	// AuthController 생성에 필요. 이 테스트에서는 쓰지 않는다
	@MockitoBean
	private SignupService signupService;

	@MockitoBean
	private LoginService loginService;

	@MockitoBean
	private RefreshTokenService refreshTokenService;

	@Test
	void 쿠키의_토큰으로_재발급하고_새_Access와_새_refresh_쿠키를_응답한다() throws Exception {
		given(tokenRefreshService.refresh("old-refresh")).willReturn(new TokenRefreshResult("new.access", 1800, "new-refresh"));

		mockMvc.perform(post(REFRESH_URL).cookie(OLD_COOKIE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value("new.access"))
			.andExpect(jsonPath("$.expiresIn").value(1800))
			// 새 refresh 토큰 원문은 본문에 없고 쿠키로만 나간다
			.andExpect(jsonPath("$.refreshToken").doesNotExist())
			.andExpect(header().string(HttpHeaders.SET_COOKIE, allOf(
				containsString("refresh_token=new-refresh"), containsString("Path=/api/v1/auth"),
				containsString("HttpOnly"), containsString("SameSite=Lax"), containsString("Max-Age=1209600"))));
	}

	@Test
	void 만료된_Access_Token이_함께_와도_재발급할_수_있다() throws Exception {
		given(tokenRefreshService.refresh("old-refresh")).willReturn(new TokenRefreshResult("new.access", 1800, "new-refresh"));
		String expired = TestJwtConfig.providerAt(TestJwtConfig.NOW.minus(Duration.ofHours(1)))
			.issueAccessToken(UUID.randomUUID(), "USER");

		mockMvc.perform(post(REFRESH_URL).cookie(OLD_COOKIE).header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
			.andExpect(status().isOk());
	}

	@Test
	void 토큰이_유효하지_않으면_401_REFRESH_TOKEN_INVALID이고_쿠키를_주지_않는다() throws Exception {
		given(tokenRefreshService.refresh(any())).willThrow(new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

		mockMvc.perform(post(REFRESH_URL).cookie(OLD_COOKIE))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("REFRESH_TOKEN_INVALID"))
			.andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
	}

	@Test
	void 쿠키가_없으면_null로_서비스에_넘기고_401() throws Exception {
		given(tokenRefreshService.refresh(isNull())).willThrow(new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

		mockMvc.perform(post(REFRESH_URL))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("REFRESH_TOKEN_INVALID"));
		verify(tokenRefreshService).refresh(isNull());
	}

}
