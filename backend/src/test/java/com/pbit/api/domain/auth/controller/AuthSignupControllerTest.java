package com.pbit.api.domain.auth.controller;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
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

import com.pbit.api.domain.auth.service.SignupResult;
import com.pbit.api.domain.auth.service.SignupService;
import com.pbit.api.global.config.CorsConfig;
import com.pbit.api.global.config.SecurityConfig;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;
import com.pbit.api.global.error.ErrorResponseWriter;
import com.pbit.api.support.TestCookieConfig;
import com.pbit.api.support.TestJwtConfig;

// POST /auth/signup 웹 계층: 요청 검증, 201 응답, Set-Cookie, 에러 코드 → HTTP 상태. 서비스는 목이다.
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, CorsConfig.class, ErrorResponseWriter.class, TestJwtConfig.class, TestCookieConfig.class})
class AuthSignupControllerTest {

	private static final UUID USER_ID = UUID.fromString("5b1c0000-0000-0000-0000-000000000001");
	private static final String VALID_BODY = """
			{
			  "email": "dohyun@example.com",
			  "password": "P@ssw0rd!",
			  "passwordConfirm": "P@ssw0rd!",
			  "nickname": "도현",
			  "agreements": [
			    { "termCode": "SERVICE", "agreed": true },
			    { "termCode": "PRIVACY", "agreed": true },
			    { "termCode": "MARKETING", "agreed": false }
			  ]
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SignupService signupService;

	@Test
	void 가입_성공시_201과_토큰_본문_refresh_쿠키를_응답한다() throws Exception {
		given(signupService.signup(any())).willReturn(new SignupResult(USER_ID, "access.jwt", 1800, false, "raw-refresh"));

		signup(VALID_BODY)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId").value(USER_ID.toString()))
				.andExpect(jsonPath("$.accessToken").value("access.jwt"))
				.andExpect(jsonPath("$.expiresIn").value(1800))
				.andExpect(jsonPath("$.onboardingCompleted").value(false))
				// refresh 토큰 원문은 본문에 없고 쿠키로만 나간다
				.andExpect(jsonPath("$.refreshToken").doesNotExist())
				.andExpect(header().string(HttpHeaders.SET_COOKIE, allOf(
						containsString("refresh_token=raw-refresh"), containsString("Path=/api/v1/auth"),
						containsString("HttpOnly"), containsString("SameSite=Lax"), containsString("Max-Age=1209600"))));
	}

	@Test
	void 만료된_토큰이_함께_와도_가입할_수_있다() throws Exception {
		given(signupService.signup(any())).willReturn(new SignupResult(USER_ID, "access.jwt", 1800, false, "raw-refresh"));

		// 만료된 토큰이 함께 와도 가입은 막지 않는다
		String expired = TestJwtConfig.providerAt(TestJwtConfig.NOW.minus(Duration.ofHours(1))).issueAccessToken(USER_ID, "USER");
		mockMvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
				.andExpect(status().isCreated());
	}

	@Test
	void 형식이_틀린_필드는_fieldErrors로_모두_응답하고_서비스를_호출하지_않는다() throws Exception {
		String body = """
				{
				  "email": "not-email",
				  "password": "short",
				  "passwordConfirm": "short",
				  "nickname": "도",
				  "agreements": []
				}
				""";

		signup(body)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.error.fieldErrors[*].field", hasItem("email")))
				.andExpect(jsonPath("$.error.fieldErrors[*].field", hasItem("password")))
				.andExpect(jsonPath("$.error.fieldErrors[*].field", hasItem("nickname")))
				// 정규식 대신 화면에 쓸 수 있는 문구가 나간다
				.andExpect(jsonPath("$.error.fieldErrors[*].reason", hasItem("8~20자 영문, 숫자, 특수문자를 포함해주세요")))
				.andExpect(jsonPath("$.error.fieldErrors[*].reason", hasItem("올바른 이메일 형식이 아니에요")))
				.andExpect(jsonPath("$.error.fieldErrors[*].reason", hasItem("2~10자로 입력해주세요")));
		verify(signupService, never()).signup(any());
	}

	@Test
	void 비밀번호_규칙_위반은_password_필드_에러() throws Exception {
		// 특수문자 없음, 숫자 없음, 공백 포함, 21자
		for (String password : new String[] {"Passw0rdd", "P@ssword!", "P@ss w0rd!", "P@ssw0rd!P@ssw0rd!123"}) {
			signup(VALID_BODY.replace("P@ssw0rd!", password))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.error.fieldErrors[*].field", hasItem("password")));
		}
	}

	@Test
	void 필수_값이_없으면_VALIDATION_ERROR() throws Exception {
		signup("{}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.error.fieldErrors[*].field", hasItem("agreements")));
	}

	@Test
	void 없는_약관_코드면_VALIDATION_ERROR() throws Exception {
		signup(VALID_BODY.replace("\"MARKETING\"", "\"UNKNOWN\""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
	}

	@Test
	void 서비스_에러_코드는_명세의_HTTP_상태로_응답한다() throws Exception {
		assertServiceError(ErrorCode.PASSWORD_MISMATCH, 400);
		assertServiceError(ErrorCode.REQUIRED_TERMS_NOT_AGREED, 400);
		assertServiceError(ErrorCode.EMAIL_DUPLICATED, 409);
		assertServiceError(ErrorCode.NICKNAME_DUPLICATED, 409);
	}

	private void assertServiceError(ErrorCode errorCode, int status) throws Exception {
		willThrow(new BusinessException(errorCode)).given(signupService).signup(any());

		signup(VALID_BODY)
				.andExpect(status().is(status))
				.andExpect(jsonPath("$.error.code").value(errorCode.name()))
				.andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
	}

	private ResultActions signup(String body) throws Exception {
		return mockMvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body));
	}

}
