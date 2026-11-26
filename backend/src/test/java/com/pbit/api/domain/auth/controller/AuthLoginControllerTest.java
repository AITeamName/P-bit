package com.pbit.api.domain.auth.controller;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.pbit.api.domain.auth.service.LoginResult;
import com.pbit.api.domain.auth.service.LoginService;
import com.pbit.api.domain.auth.service.RefreshTokenService;
import com.pbit.api.domain.auth.service.SignupService;
import com.pbit.api.domain.auth.service.TokenRefreshService;
import com.pbit.api.global.config.CorsConfig;
import com.pbit.api.global.config.SecurityConfig;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;
import com.pbit.api.global.error.ErrorResponseWriter;
import com.pbit.api.support.TestCookieConfig;
import com.pbit.api.support.TestJwtConfig;

// POST /auth/login 웹 계층: 요청 검증, 200 응답, Set-Cookie, 인증 없이 호출 가능. 서비스는 목이다.
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, CorsConfig.class, ErrorResponseWriter.class, TestJwtConfig.class, TestCookieConfig.class})
class AuthLoginControllerTest {

	private static final UUID USER_ID = UUID.fromString("5b1c0000-0000-0000-0000-000000000001");
	private static final String VALID_BODY = """
			{ "email": "dohyun@example.com", "password": "P@ssw0rd!" }
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private LoginService loginService;

	@MockitoBean
	private RefreshTokenService refreshTokenService;

	// AuthController 생성에 필요. 이 테스트에서는 쓰지 않는다
	@MockitoBean
	private SignupService signupService;

	// AuthController 생성에 필요. 이 테스트에서는 쓰지 않는다
	@MockitoBean
	private TokenRefreshService tokenRefreshService;

	@Test
	void 로그인_성공시_200과_토큰_본문_refresh_쿠키를_응답한다() throws Exception {
		given(loginService.login(any())).willReturn(new LoginResult(USER_ID, "access.jwt", 1800, true, "raw-refresh"));

		login(VALID_BODY)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value(USER_ID.toString()))
			.andExpect(jsonPath("$.accessToken").value("access.jwt"))
			.andExpect(jsonPath("$.expiresIn").value(1800))
			.andExpect(jsonPath("$.onboardingCompleted").value(true))
			// refresh 토큰 원문은 본문에 없고 쿠키로만 나간다
			.andExpect(jsonPath("$.refreshToken").doesNotExist())
			.andExpect(header().string(HttpHeaders.SET_COOKIE, allOf(
				containsString("refresh_token=raw-refresh"), containsString("Path=/api/v1/auth"),
				containsString("HttpOnly"), containsString("SameSite=Lax"), containsString("Max-Age=1209600"))));
	}

	@Test
	void 이메일이나_비밀번호가_틀리면_401_INVALID_CREDENTIALS() throws Exception {
		given(loginService.login(any())).willThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		login(VALID_BODY)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"))
			.andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
	}

	@Test
	void 이메일이나_비밀번호가_비어_있으면_400이고_서비스를_호출하지_않는다() throws Exception {
		login("""
				{ "email": " ", "password": "" }
				""")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
			.andExpect(jsonPath("$.error.fieldErrors[*].field", hasItem("email")))
			.andExpect(jsonPath("$.error.fieldErrors[*].field", hasItem("password")));
		verify(loginService, never()).login(any());
	}

	@Test
	void 형식이_틀린_이메일도_검증하지_않고_서비스로_넘긴다() throws Exception {
		// 형식 오류를 따로 알려주면 실패 원인이 드러난다. 서비스가 INVALID_CREDENTIALS로 같은 응답을 준다
		given(loginService.login(any())).willThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		login("""
				{ "email": "not-email", "password": "P@ssw0rd!" }
				""")
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void 만료된_토큰이_함께_와도_로그인할_수_있다() throws Exception {
		given(loginService.login(any())).willReturn(new LoginResult(USER_ID, "access.jwt", 1800, true, "raw-refresh"));
		String expired = TestJwtConfig.providerAt(TestJwtConfig.NOW.minus(Duration.ofHours(1)))
			.issueAccessToken(USER_ID, "USER");

		mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
			.andExpect(status().isOk());
	}

	private ResultActions login(String body) throws Exception {
		return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body));
	}

}
