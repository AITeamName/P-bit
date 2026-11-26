package com.pbit.api.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.pbit.api.domain.auth.entity.ClientType;
import com.pbit.api.domain.user.entity.User;
import com.pbit.api.domain.user.repository.UserRepository;
import com.pbit.api.global.auth.AuthUser;
import com.pbit.api.global.auth.JwtProperties;
import com.pbit.api.global.auth.JwtTokenProvider;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;
import com.pbit.api.support.TestJwtConfig;

import jakarta.persistence.EntityManager;

// 실제 PostgreSQL에 회원과 Refresh Token을 저장해 두고 재발급한다. 테스트마다 롤백된다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TokenRefreshService.class, RefreshTokenService.class, TestJwtConfig.class, TokenRefreshServiceTest.Config.class})
class TokenRefreshServiceTest {

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

	@Autowired
	private TokenRefreshService tokenRefreshService;

	@Autowired
	private RefreshTokenService refreshTokenService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManager entityManager;

	private User user;
	private String oldRefreshToken;

	@BeforeEach
	void 회원과_Refresh_Token을_저장한다() {
		user = userRepository.saveAndFlush(
			User.createLocal("dohyun@example.com", "$2a$10$hash", "도현", TestJwtConfig.NOW));
		oldRefreshToken = refreshTokenService.issue(user.getId(), ClientType.WEB).rawToken();
	}

	@Test
	void 재발급하면_새_Access_Token과_새_Refresh_Token을_준다() {
		TokenRefreshResult result = tokenRefreshService.refresh(oldRefreshToken);

		AuthUser authUser = jwtTokenProvider.parse(result.accessToken());
		assertThat(authUser.userId()).isEqualTo(user.getId());
		assertThat(authUser.role()).isEqualTo("USER");
		assertThat(result.expiresIn()).isEqualTo(1800);
		assertThat(result.refreshToken()).isNotEqualTo(oldRefreshToken);
	}

	@Test
	void 재발급하면_이전_Refresh_Token은_폐기되고_새_토큰은_같은_기기로_저장된다() {
		TokenRefreshResult result = tokenRefreshService.refresh(oldRefreshToken);

		assertThat(revokedAtOf(oldRefreshToken)).isNotNull();
		Map<String, Object> saved = rowOf(result.refreshToken());
		assertThat(saved.get("client_type")).isEqualTo("WEB");
		assertThat(saved.get("revoked_at")).isNull();
	}

	@Test
	void 이미_쓴_Refresh_Token으로_다시_재발급하면_REFRESH_TOKEN_INVALID() {
		tokenRefreshService.refresh(oldRefreshToken);

		assertRefreshTokenInvalid(oldRefreshToken);
	}

	@Test
	void 로그아웃한_Refresh_Token으로는_재발급할_수_없다() {
		refreshTokenService.revoke(oldRefreshToken);

		assertRefreshTokenInvalid(oldRefreshToken);
	}

	@Test
	void 없거나_빈_토큰이면_REFRESH_TOKEN_INVALID() {
		assertRefreshTokenInvalid("not-issued-token");
		assertRefreshTokenInvalid(null);
		assertRefreshTokenInvalid(" ");
	}

	@Test
	void 탈퇴한_회원이면_REFRESH_TOKEN_INVALID() {
		jdbcTemplate.update("UPDATE users SET deleted_at = ? WHERE id = ?",
			Timestamp.from(TestJwtConfig.NOW), user.getId());
		// SQL로 바꾼 값을 다시 읽도록 JPA가 들고 있는 객체를 비운다
		entityManager.clear();

		assertRefreshTokenInvalid(oldRefreshToken);
	}

	private void assertRefreshTokenInvalid(String rawToken) {
		assertThatThrownBy(() -> tokenRefreshService.refresh(rawToken))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException) e).getErrorCode())
			.isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID);
	}

	private Object revokedAtOf(String rawToken) {
		return rowOf(rawToken).get("revoked_at");
	}

	private Map<String, Object> rowOf(String rawToken) {
		return jdbcTemplate.queryForMap("SELECT client_type, revoked_at FROM refresh_tokens WHERE token_hash = ?",
			RefreshTokenHasher.hash(rawToken));
	}

}
