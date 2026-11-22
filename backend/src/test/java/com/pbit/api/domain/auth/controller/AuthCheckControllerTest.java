package com.pbit.api.domain.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.pbit.api.domain.auth.service.LoginService;
import com.pbit.api.domain.auth.service.TokenRefreshService;
import com.pbit.api.domain.auth.service.RefreshTokenService;
import com.pbit.api.domain.auth.service.SignupService;
import com.pbit.api.domain.term.repository.TermRepository;
import com.pbit.api.domain.user.repository.UserRepository;
import com.pbit.api.domain.user.repository.UserTermAgreementRepository;
import com.pbit.api.global.config.ClockConfig;
import com.pbit.api.global.config.CorsConfig;
import com.pbit.api.global.config.PasswordEncoderConfig;
import com.pbit.api.global.config.SecurityConfig;
import com.pbit.api.global.error.ErrorResponseWriter;
import com.pbit.api.support.TestCookieConfig;
import com.pbit.api.support.TestJwtConfig;

// 이메일·닉네임 중복 확인. 리포지토리는 목이라 DB 없이 실행된다. 모든 요청을 인증 없이 보낸다.
@WebMvcTest(controllers = AuthController.class)
@Import({SignupService.class, SecurityConfig.class, CorsConfig.class, ErrorResponseWriter.class, TestJwtConfig.class,
		PasswordEncoderConfig.class, ClockConfig.class, TestCookieConfig.class})
class AuthCheckControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserRepository userRepository;

	// 회원가입(SignupService.signup)에서만 쓰는 의존성
	@MockitoBean
	private UserTermAgreementRepository userTermAgreementRepository;

	@MockitoBean
	private TermRepository termRepository;

	@MockitoBean
	private RefreshTokenService refreshTokenService;

	// AuthController 생성에 필요. 이 테스트에서는 쓰지 않는다
	@MockitoBean
	private LoginService loginService;

	// AuthController 생성에 필요. 이 테스트에서는 쓰지 않는다
	@MockitoBean
	private TokenRefreshService tokenRefreshService;

	@Test
	void 가입되지_않은_이메일이면_available_true() throws Exception {
		mockMvc.perform(get("/api/v1/auth/check-email").param("email", "new@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(true));
	}

	@Test
	void 이미_가입된_이메일이면_available_false() throws Exception {
		given(userRepository.existsByEmail("dohyun@example.com")).willReturn(true);

		mockMvc.perform(get("/api/v1/auth/check-email").param("email", "dohyun@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(false));
	}

	@Test
	void 대소문자만_다른_이메일도_중복으로_본다() throws Exception {
		given(userRepository.existsByEmail("dohyun@example.com")).willReturn(true);

		mockMvc.perform(get("/api/v1/auth/check-email").param("email", "DoHyun@Example.COM"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(false));
	}

	@Test
	void 이메일_형식이_틀리면_400_VALIDATION_ERROR() throws Exception {
		for (String invalid : new String[] {"not-email", "a@b", "a b@example.com", " a@example.com", ""}) {
			mockMvc.perform(get("/api/v1/auth/check-email").param("email", invalid))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
					.andExpect(jsonPath("$.error.fieldErrors[0].field").value("email"));
		}
	}

	@Test
	void 이메일_파라미터가_없으면_400_VALIDATION_ERROR() throws Exception {
		mockMvc.perform(get("/api/v1/auth/check-email"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.error.fieldErrors[0].field").value("email"));
	}

	@Test
	void 사용_가능한_닉네임이면_available_true() throws Exception {
		mockMvc.perform(get("/api/v1/auth/check-nickname").param("nickname", "도현"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(true));
	}

	@Test
	void 이미_사용_중인_닉네임이면_available_false() throws Exception {
		given(userRepository.existsByNickname("도현")).willReturn(true);

		mockMvc.perform(get("/api/v1/auth/check-nickname").param("nickname", "도현"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(false));
	}

	@Test
	void 닉네임_길이가_2에서_10자를_벗어나면_400_VALIDATION_ERROR() throws Exception {
		for (String invalid : new String[] {"도", "열한글자닉네임입니다요", " 도현", "도현 "}) {
			mockMvc.perform(get("/api/v1/auth/check-nickname").param("nickname", invalid))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
					.andExpect(jsonPath("$.error.fieldErrors[0].field").value("nickname"));
		}
	}

	@Test
	void 닉네임_경계값_2자와_10자는_통과한다() throws Exception {
		for (String valid : new String[] {"도현", "열글자닉네임입니다"}) {
			mockMvc.perform(get("/api/v1/auth/check-nickname").param("nickname", valid))
					.andExpect(status().isOk());
		}
	}

}
