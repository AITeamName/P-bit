package com.pbit.api.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willReturn;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.pbit.api.domain.auth.dto.SignupRequest;
import com.pbit.api.domain.auth.dto.SignupRequest.Agreement;
import com.pbit.api.domain.term.entity.TermCode;
import com.pbit.api.domain.user.repository.UserRepository;
import com.pbit.api.global.auth.AuthUser;
import com.pbit.api.global.auth.JwtProperties;
import com.pbit.api.global.auth.JwtTokenProvider;
import com.pbit.api.global.config.PasswordEncoderConfig;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;
import com.pbit.api.support.TestJwtConfig;

// 실제 PostgreSQL(Flyway 스키마 + V2 약관 seed)에 붙어서 저장 내용을 확인한다. 테스트마다 롤백된다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SignupService.class, RefreshTokenService.class, PasswordEncoderConfig.class, TestJwtConfig.class,
		SignupServiceTest.Config.class})
class SignupServiceTest {

	@TestConfiguration
	static class Config {

		@Bean
		Clock clock() {
			return Clock.fixed(TestJwtConfig.NOW, ZoneOffset.UTC);
		}

		@Bean
		JwtProperties jwtProperties() {
			return TestJwtConfig.PROPERTIES;
		}

	}

	private static final String PASSWORD = "P@ssw0rd!";

	@Autowired
	private SignupService signupService;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockitoSpyBean
	private UserRepository userRepository;

	@Test
	void 가입하면_회원과_약관_동의가_저장되고_토큰이_발급된다() {
		SignupResult result = signupService.signup(request("DoHyun@Example.com", "도현", true, true, false));

		Map<String, Object> user = jdbcTemplate.queryForMap("SELECT * FROM users WHERE id = ?", result.userId());
		assertThat(user.get("email")).isEqualTo("dohyun@example.com");
		assertThat(user.get("nickname")).isEqualTo("도현");
		assertThat(user.get("onboarding_completed")).isEqualTo(false);
		// 비밀번호는 BCrypt 해시로만 저장된다
		String hash = (String) user.get("password_hash");
		assertThat(hash).isNotEqualTo(PASSWORD).startsWith("$2");
		assertThat(passwordEncoder.matches(PASSWORD, hash)).isTrue();

		List<Map<String, Object>> agreements = jdbcTemplate.queryForList("""
				SELECT t.code, a.agreed FROM user_term_agreements a JOIN terms t ON t.id = a.term_id
				WHERE a.user_id = ? ORDER BY t.id""", result.userId());
		assertThat(agreements).extracting(row -> row.get("code") + ":" + row.get("agreed"))
				.containsExactly("SERVICE:true", "PRIVACY:true", "MARKETING:false");

		AuthUser tokenUser = jwtTokenProvider.parse(result.accessToken());
		assertThat(tokenUser.userId()).isEqualTo(result.userId());
		assertThat(tokenUser.role()).isEqualTo("USER");
		assertThat(result.expiresIn()).isEqualTo(1800);
		assertThat(result.onboardingCompleted()).isFalse();
		assertThat(result.refreshToken()).isNotBlank();
		assertThat(jdbcTemplate.queryForObject("SELECT client_type FROM refresh_tokens WHERE user_id = ?",
				String.class, result.userId())).isEqualTo("WEB");
	}

	@Test
	void 요청에서_빠진_선택_약관은_미동의로_저장된다() {
		SignupRequest request = new SignupRequest("a@example.com", PASSWORD, PASSWORD, "닉네임",
				List.of(new Agreement(TermCode.SERVICE, true), new Agreement(TermCode.PRIVACY, true)));

		UUID userId = signupService.signup(request).userId();

		assertThat(jdbcTemplate.queryForObject("""
				SELECT a.agreed FROM user_term_agreements a JOIN terms t ON t.id = a.term_id
				WHERE a.user_id = ? AND t.code = 'MARKETING'""", Boolean.class, userId)).isFalse();
	}

	@Test
	void 비밀번호_확인이_다르면_PASSWORD_MISMATCH() {
		SignupRequest request = new SignupRequest("a@example.com", PASSWORD, "Other1234!", "닉네임", allAgreed());

		assertError(request, ErrorCode.PASSWORD_MISMATCH);
	}

	@Test
	void 필수_약관에_동의하지_않으면_REQUIRED_TERMS_NOT_AGREED() {
		assertError(request("a@example.com", "닉네임", true, false, true), ErrorCode.REQUIRED_TERMS_NOT_AGREED);
	}

	@Test
	void 필수_약관이_요청에서_빠져도_REQUIRED_TERMS_NOT_AGREED() {
		SignupRequest request = new SignupRequest("a@example.com", PASSWORD, PASSWORD, "닉네임",
				List.of(new Agreement(TermCode.SERVICE, true)));

		assertError(request, ErrorCode.REQUIRED_TERMS_NOT_AGREED);
	}

	@Test
	void 가입된_이메일이면_대소문자가_달라도_EMAIL_DUPLICATED() {
		signupService.signup(request("dohyun@example.com", "도현", true, true, false));

		assertError(request("DOHYUN@example.com", "다른닉", true, true, false), ErrorCode.EMAIL_DUPLICATED);
	}

	@Test
	void 사용_중인_닉네임이면_NICKNAME_DUPLICATED() {
		signupService.signup(request("dohyun@example.com", "도현", true, true, false));

		assertError(request("other@example.com", "도현", true, true, false), ErrorCode.NICKNAME_DUPLICATED);
	}

	@Test
	void 중복_확인을_통과했지만_동시_가입으로_이메일_UNIQUE에_걸리면_EMAIL_DUPLICATED() {
		signupService.signup(request("dohyun@example.com", "도현", true, true, false));
		// 다른 요청이 확인과 저장 사이에 먼저 가입한 상황: 중복 확인은 통과시키고 DB 제약에서 걸리게 한다
		willReturn(false).given(userRepository).existsByEmail(anyString());

		assertError(request("dohyun@example.com", "다른닉", true, true, false), ErrorCode.EMAIL_DUPLICATED);
	}

	@Test
	void 중복_확인을_통과했지만_동시_가입으로_닉네임_UNIQUE에_걸리면_NICKNAME_DUPLICATED() {
		signupService.signup(request("dohyun@example.com", "도현", true, true, false));
		willReturn(false).given(userRepository).existsByNickname(anyString());

		assertError(request("other@example.com", "도현", true, true, false), ErrorCode.NICKNAME_DUPLICATED);
	}

	private void assertError(SignupRequest request, ErrorCode expected) {
		assertThatThrownBy(() -> signupService.signup(request))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode").isEqualTo(expected);
	}

	private static SignupRequest request(String email, String nickname, boolean service, boolean privacy, boolean marketing) {
		return new SignupRequest(email, PASSWORD, PASSWORD, nickname, List.of(
				new Agreement(TermCode.SERVICE, service),
				new Agreement(TermCode.PRIVACY, privacy),
				new Agreement(TermCode.MARKETING, marketing)));
	}

	private static List<Agreement> allAgreed() {
		return List.of(new Agreement(TermCode.SERVICE, true), new Agreement(TermCode.PRIVACY, true),
				new Agreement(TermCode.MARKETING, true));
	}

}
