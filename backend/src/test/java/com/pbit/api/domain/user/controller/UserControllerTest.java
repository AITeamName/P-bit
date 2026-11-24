package com.pbit.api.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.pbit.api.domain.user.dto.response.UserMeResponse;
import com.pbit.api.domain.user.entity.AuthProvider;
import com.pbit.api.domain.user.entity.Domain;
import com.pbit.api.domain.user.entity.Level;
import com.pbit.api.domain.user.entity.SelfReportedLevel;
import com.pbit.api.domain.user.service.UserService;
import com.pbit.api.global.config.CorsConfig;
import com.pbit.api.global.config.SecurityConfig;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;
import com.pbit.api.global.error.ErrorResponseWriter;
import com.pbit.api.support.TestJwtConfig;

// GET /users/me 웹 계층: 토큰에서 꺼낸 userId로 조회하는지, 토큰 문제는 401로 막히는지. 서비스는 목이다.
@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, CorsConfig.class, ErrorResponseWriter.class, TestJwtConfig.class})
class UserControllerTest {

	private static final UUID USER_ID = UUID.fromString("5b1c0000-0000-0000-0000-000000000001");
	private static final String ME_URL = "/api/v1/users/me";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	void 토큰의_회원_정보를_200으로_응답한다() throws Exception {
		Instant measuredAt = Instant.parse("2026-08-20T05:00:00Z");
		given(userService.getUserMe(USER_ID)).willReturn(new UserMeResponse(USER_ID, "dohyun@example.com", "도현",
			AuthProvider.LOCAL, Domain.MARKETING, SelfReportedLevel.INTERMEDIATE, Level.LV2, measuredAt, true));

		mockMvc.perform(get(ME_URL).header(HttpHeaders.AUTHORIZATION, bearer(validToken())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value(USER_ID.toString()))
			.andExpect(jsonPath("$.email").value("dohyun@example.com"))
			.andExpect(jsonPath("$.nickname").value("도현"))
			.andExpect(jsonPath("$.provider").value("LOCAL"))
			.andExpect(jsonPath("$.domain").value("MARKETING"))
			.andExpect(jsonPath("$.selfReportedLevel").value("INTERMEDIATE"))
			.andExpect(jsonPath("$.currentLevel").value("LV2"))
			.andExpect(jsonPath("$.levelMeasuredAt").value("2026-08-20T05:00:00Z"))
			.andExpect(jsonPath("$.onboardingCompleted").value(true));
	}

	@Test
	void 온보딩_전_회원은_레벨_필드가_null이다() throws Exception {
		given(userService.getUserMe(USER_ID)).willReturn(new UserMeResponse(USER_ID, "dohyun@example.com", "도현",
			AuthProvider.LOCAL, null, null, null, null, false));

		mockMvc.perform(get(ME_URL).header(HttpHeaders.AUTHORIZATION, bearer(validToken())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.domain").isEmpty())
			.andExpect(jsonPath("$.currentLevel").isEmpty())
			.andExpect(jsonPath("$.onboardingCompleted").value(false));
	}

	@Test
	void 토큰이_없으면_401_TOKEN_INVALID이고_서비스를_호출하지_않는다() throws Exception {
		mockMvc.perform(get(ME_URL))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("TOKEN_INVALID"));
		verify(userService, never()).getUserMe(any());
	}

	@Test
	void 만료된_토큰이면_401_TOKEN_EXPIRED() throws Exception {
		String expired = TestJwtConfig.providerAt(TestJwtConfig.NOW.minus(Duration.ofHours(1)))
			.issueAccessToken(USER_ID, "USER");

		mockMvc.perform(get(ME_URL).header(HttpHeaders.AUTHORIZATION, bearer(expired)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("TOKEN_EXPIRED"));
		verify(userService, never()).getUserMe(any());
	}

	@Test
	void 탈퇴했거나_없는_회원이면_401_TOKEN_INVALID() throws Exception {
		given(userService.getUserMe(USER_ID)).willThrow(new BusinessException(ErrorCode.TOKEN_INVALID));

		mockMvc.perform(get(ME_URL).header(HttpHeaders.AUTHORIZATION, bearer(validToken())))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("TOKEN_INVALID"));
	}

	private static String validToken() {
		return TestJwtConfig.providerAt(TestJwtConfig.NOW).issueAccessToken(USER_ID, "USER");
	}

	private static String bearer(String token) {
		return "Bearer " + token;
	}

}
