package com.pbit.api.global.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbit.api.global.config.CorsConfig;
import com.pbit.api.global.config.SecurityConfig;
import com.pbit.api.global.error.ErrorResponseWriter;
import com.pbit.api.support.TestJwtConfig;

// 실제 SecurityConfig + JWT 필터로 요청을 보낸다. DB 없이 실행된다.
@WebMvcTest(controllers = JwtAuthenticationFilterTest.TestAuthController.class)
@Import({JwtAuthenticationFilterTest.TestAuthController.class, SecurityConfig.class, CorsConfig.class,
		ErrorResponseWriter.class, TestJwtConfig.class})
class JwtAuthenticationFilterTest {

	private static final UUID USER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
	private static final String PROTECTED = "/test-auth/me";
	private static final String PUBLIC = "/api/v1/terms";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Test
	void 유효한_토큰이면_인증된_사용자로_통과한다() throws Exception {
		String token = jwtTokenProvider.issueAccessToken(USER_ID, "USER");

		mockMvc.perform(get(PROTECTED).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().string(USER_ID + ":USER"));
	}

	@Test
	void 토큰이_없으면_401_TOKEN_INVALID() throws Exception {
		mockMvc.perform(get(PROTECTED))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("TOKEN_INVALID"))
				.andExpect(jsonPath("$.error.userMessage").value("다시 로그인해주세요."));
	}

	@Test
	void 만료된_토큰이면_401_TOKEN_EXPIRED() throws Exception {
		String expired = TestJwtConfig.providerAt(TestJwtConfig.NOW.minus(Duration.ofMinutes(31)))
				.issueAccessToken(USER_ID, "USER");

		mockMvc.perform(get(PROTECTED).header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("TOKEN_EXPIRED"))
				.andExpect(jsonPath("$.error.userMessage").value("로그인이 만료되었어요."))
				.andExpect(jsonPath("$.error.requestId").exists());
	}

	@Test
	void 위조된_토큰이면_401_TOKEN_INVALID() throws Exception {
		String token = jwtTokenProvider.issueAccessToken(USER_ID, "USER");
		String forged = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

		mockMvc.perform(get(PROTECTED).header(HttpHeaders.AUTHORIZATION, "Bearer " + forged))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("TOKEN_INVALID"));
	}

	@Test
	void Bearer_형식이_아니면_401_TOKEN_INVALID() throws Exception {
		String token = jwtTokenProvider.issueAccessToken(USER_ID, "USER");

		mockMvc.perform(get(PROTECTED).header(HttpHeaders.AUTHORIZATION, "Basic " + token))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("TOKEN_INVALID"));
	}

	@Test
	void 인증이_필요없는_API는_만료된_토큰이_와도_통과한다() throws Exception {
		String expired = TestJwtConfig.providerAt(TestJwtConfig.NOW.minus(Duration.ofMinutes(31)))
				.issueAccessToken(USER_ID, "USER");

		mockMvc.perform(get(PUBLIC).header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
				.andExpect(status().isOk());
	}

	@RestController
	static class TestAuthController {

		@GetMapping(PROTECTED)
		String me(@AuthenticationPrincipal AuthUser user) {
			return user.userId() + ":" + user.role();
		}

		// permitAll 경로 확인용 (실제 약관 API 대신)
		@GetMapping(PUBLIC)
		String terms() {
			return "ok";
		}

	}

}
