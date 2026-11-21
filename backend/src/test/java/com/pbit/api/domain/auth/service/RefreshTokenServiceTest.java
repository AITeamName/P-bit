package com.pbit.api.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
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

import com.pbit.api.domain.auth.entity.ClientType;
import com.pbit.api.global.auth.JwtProperties;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;
import com.pbit.api.support.MutableClock;
import com.pbit.api.support.TestJwtConfig;

// 실제 PostgreSQL(Flyway 스키마)에 붙어서 저장 내용을 확인한다. 테스트마다 롤백된다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RefreshTokenService.class, RefreshTokenServiceTest.Config.class})
class RefreshTokenServiceTest {

	@TestConfiguration
	static class Config {

		@Bean
		MutableClock clock() {
			return new MutableClock(TestJwtConfig.NOW);
		}

		@Bean
		JwtProperties jwtProperties() {
			return TestJwtConfig.PROPERTIES;
		}

	}

	@Autowired
	private RefreshTokenService refreshTokenService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private Clock clock;

	private UUID userId;

	@BeforeEach
	void setUp() {
		userId = jdbcTemplate.queryForObject(
				"INSERT INTO users (email, nickname) VALUES ('jwt-test@pbit.dev', 'jwt테스트') RETURNING id", UUID.class);
	}

	@Test
	void 발급하면_원문이_아닌_SHA256_해시만_저장된다() {
		IssuedRefreshToken issued = refreshTokenService.issue(userId, ClientType.WEB);

		Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM refresh_tokens WHERE user_id = ?", userId);
		assertThat(row.get("token_hash")).isEqualTo(RefreshTokenHasher.hash(issued.rawToken()));
		assertThat((String) row.get("token_hash")).hasSize(64).matches("[0-9a-f]{64}");
		// 어떤 컬럼에도 원문이 들어있지 않다
		assertThat(row.values()).noneMatch(value -> value != null && value.toString().contains(issued.rawToken()));
		assertThat(row.get("client_type")).isEqualTo("WEB");
		assertThat(row.get("revoked_at")).isNull();
	}

	@Test
	void 만료_시각은_발급_시각에서_14일_뒤다() {
		IssuedRefreshToken issued = refreshTokenService.issue(userId, ClientType.WEB);

		assertThat(issued.expiresAt()).isEqualTo(TestJwtConfig.NOW.plus(Duration.ofDays(14)));
	}

	@Test
	void 매번_다른_토큰이_발급된다() {
		String first = refreshTokenService.issue(userId, ClientType.WEB).rawToken();
		String second = refreshTokenService.issue(userId, ClientType.WEB).rawToken();

		assertThat(first).isNotEqualTo(second);
		assertThat(first).hasSizeGreaterThanOrEqualTo(43); // 32바이트 base64url
	}

	@Test
	void 재발급하면_기존_토큰은_폐기되고_새_토큰이_나온다() {
		IssuedRefreshToken first = refreshTokenService.issue(userId, ClientType.EXTENSION);

		IssuedRefreshToken rotated = refreshTokenService.rotate(first.rawToken());

		assertThat(rotated.rawToken()).isNotEqualTo(first.rawToken());
		assertThat(rotated.userId()).isEqualTo(userId);
		assertThat(rotated.clientType()).isEqualTo(ClientType.EXTENSION);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"SELECT token_hash, revoked_at FROM refresh_tokens WHERE user_id = ? ORDER BY id", userId);
		assertThat(rows).hasSize(2);
		assertThat(rows.get(0).get("revoked_at")).isNotNull();
		assertThat(rows.get(1).get("revoked_at")).isNull();
	}

	@Test
	void 이미_사용한_토큰으로_다시_재발급하면_REFRESH_TOKEN_INVALID() {
		IssuedRefreshToken first = refreshTokenService.issue(userId, ClientType.WEB);
		refreshTokenService.rotate(first.rawToken());

		assertInvalid(first.rawToken());
	}

	@Test
	void 만료된_토큰으로_재발급하면_REFRESH_TOKEN_INVALID() {
		IssuedRefreshToken issued = refreshTokenService.issue(userId, ClientType.WEB);
		((MutableClock) clock).advance(Duration.ofDays(14).plusSeconds(1));

		assertInvalid(issued.rawToken());
	}

	@Test
	void 없는_토큰이나_빈_값이면_REFRESH_TOKEN_INVALID() {
		assertInvalid("unknown-token");
		assertInvalid("");
		assertInvalid(null);
	}

	@Test
	void 로그아웃으로_폐기한_토큰은_재발급할_수_없다() {
		IssuedRefreshToken issued = refreshTokenService.issue(userId, ClientType.WEB);

		refreshTokenService.revoke(issued.rawToken());

		assertInvalid(issued.rawToken());
	}

	@Test
	void 없는_토큰을_폐기해도_예외가_나지_않는다() {
		refreshTokenService.revoke("unknown-token");
		refreshTokenService.revoke(null);
	}

	private void assertInvalid(String rawToken) {
		assertThatThrownBy(() -> refreshTokenService.rotate(rawToken))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode").isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID);
	}

}
