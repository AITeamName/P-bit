package com.pbit.api.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pbit.api.domain.auth.dto.request.LoginRequest;
import com.pbit.api.domain.user.entity.User;
import com.pbit.api.domain.user.repository.UserRepository;
import com.pbit.api.global.auth.AuthUser;
import com.pbit.api.global.auth.JwtProperties;
import com.pbit.api.global.auth.JwtTokenProvider;
import com.pbit.api.global.config.PasswordEncoderConfig;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;
import com.pbit.api.support.TestJwtConfig;

import jakarta.persistence.EntityManager;

// 실제 PostgreSQL에 회원을 저장해 두고 로그인한다. 테스트마다 롤백된다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({LoginService.class, RefreshTokenService.class, PasswordEncoderConfig.class, TestJwtConfig.class,
		LoginServiceTest.Config.class})
class LoginServiceTest {

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

	private static final String EMAIL = "dohyun@example.com";
	private static final String PASSWORD = "P@ssw0rd!";

	@Autowired
	private LoginService loginService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManager entityManager;

	private User user;

	@BeforeEach
	void 회원을_저장한다() {
		user = userRepository.saveAndFlush(
			User.createLocal(EMAIL, passwordEncoder.encode(PASSWORD), "도현", TestJwtConfig.NOW));
	}

	@Test
	void 로그인하면_토큰이_발급되고_웹_Refresh_Token이_저장된다() {
		LoginResult result = loginService.login(new LoginRequest(EMAIL, PASSWORD));

		assertThat(result.userId()).isEqualTo(user.getId());
		assertThat(result.expiresIn()).isEqualTo(1800);
		assertThat(result.onboardingCompleted()).isFalse();

		AuthUser authUser = jwtTokenProvider.parse(result.accessToken());
		assertThat(authUser.userId()).isEqualTo(user.getId());
		assertThat(authUser.role()).isEqualTo("USER");

		Map<String, Object> saved = jdbcTemplate.queryForMap(
			"SELECT client_type, revoked_at FROM refresh_tokens WHERE token_hash = ?",
			RefreshTokenHasher.hash(result.refreshToken()));
		assertThat(saved.get("client_type")).isEqualTo("WEB");
		assertThat(saved.get("revoked_at")).isNull();
	}

	@Test
	void 이메일은_대소문자를_구분하지_않는다() {
		LoginResult result = loginService.login(new LoginRequest("DoHyun@Example.COM", PASSWORD));

		assertThat(result.userId()).isEqualTo(user.getId());
	}

	@Test
	void 가입하지_않은_이메일이면_INVALID_CREDENTIALS() {
		assertInvalidCredentials(new LoginRequest("nobody@example.com", PASSWORD));
	}

	@Test
	void 비밀번호가_틀리면_INVALID_CREDENTIALS() {
		assertInvalidCredentials(new LoginRequest(EMAIL, "Wr0ng!pass"));
	}

	@Test
	void 탈퇴한_회원이면_INVALID_CREDENTIALS() {
		jdbcTemplate.update("UPDATE users SET deleted_at = ? WHERE id = ?",
			Timestamp.from(TestJwtConfig.NOW), user.getId());
		// SQL로 바꾼 값은 JPA가 들고 있는 회원 객체에 반영되지 않는다. 비워서 DB에서 다시 읽게 한다
		entityManager.clear();

		assertInvalidCredentials(new LoginRequest(EMAIL, PASSWORD));
	}

	@Test
	void 비밀번호가_없는_소셜_가입_회원이면_INVALID_CREDENTIALS() {
		UUID googleUserId = UUID.randomUUID();
		jdbcTemplate.update("""
				INSERT INTO users (id, email, nickname, provider, provider_id)
				VALUES (?, 'google@example.com', '구글회원', 'GOOGLE', 'google-sub-1')
				""", googleUserId);

		assertInvalidCredentials(new LoginRequest("google@example.com", PASSWORD));
	}

	@Test
	void 실패하면_Refresh_Token을_저장하지_않는다() {
		Integer before = jdbcTemplate.queryForObject("SELECT count(*) FROM refresh_tokens", Integer.class);

		assertInvalidCredentials(new LoginRequest(EMAIL, "Wr0ng!pass"));

		Integer after = jdbcTemplate.queryForObject("SELECT count(*) FROM refresh_tokens", Integer.class);
		assertThat(after).isEqualTo(before);
	}

	private void assertInvalidCredentials(LoginRequest request) {
		assertThatThrownBy(() -> loginService.login(request))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException) e).getErrorCode())
			.isEqualTo(ErrorCode.INVALID_CREDENTIALS);
	}

}
