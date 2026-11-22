package com.pbit.api.global.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

// 스프링 없이 도는 단위 테스트
class JwtTokenProviderTest {

	private static final String SECRET = "test-only-secret-key-at-least-32-bytes-long!!";
	private static final Instant NOW = Instant.parse("2026-11-15T00:00:00Z");
	private static final UUID USER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

	private final JwtProperties properties = new JwtProperties(SECRET, Duration.ofMinutes(30), Duration.ofDays(14));

	private JwtTokenProvider providerAt(Instant instant) {
		return new JwtTokenProvider(properties, Clock.fixed(instant, ZoneOffset.UTC));
	}

	@Test
	void 발급한_토큰을_검증하면_사용자_정보가_나온다() {
		JwtTokenProvider provider = providerAt(NOW);

		AuthUser user = provider.parse(provider.issueAccessToken(USER_ID, "USER"));

		assertThat(user.userId()).isEqualTo(USER_ID);
		assertThat(user.role()).isEqualTo("USER");
		assertThat(provider.accessTokenExpiresInSeconds()).isEqualTo(1800);
	}

	@Test
	void 만료_직전_토큰은_통과한다() {
		String token = providerAt(NOW).issueAccessToken(USER_ID, "USER");

		AuthUser user = providerAt(NOW.plus(Duration.ofMinutes(29))).parse(token);

		assertThat(user.userId()).isEqualTo(USER_ID);
	}

	@Test
	void 만료된_토큰은_TOKEN_EXPIRED() {
		String token = providerAt(NOW).issueAccessToken(USER_ID, "USER");

		assertThatThrownBy(() -> providerAt(NOW.plus(Duration.ofMinutes(31))).parse(token))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode").isEqualTo(ErrorCode.TOKEN_EXPIRED);
	}

	@Test
	void 다른_키로_서명한_토큰은_TOKEN_INVALID() {
		String forged = Jwts.builder()
				.subject(USER_ID.toString())
				.claim("role", "ADMIN")
				.expiration(Date.from(NOW.plus(Duration.ofMinutes(30))))
				.signWith(Keys.hmacShaKeyFor("another-secret-key-at-least-32-bytes-long!!".getBytes(StandardCharsets.UTF_8)))
				.compact();

		assertInvalid(forged);
	}

	@Test
	void 페이로드를_변조한_토큰은_TOKEN_INVALID() {
		String[] parts = providerAt(NOW).issueAccessToken(USER_ID, "USER").split("\\.");
		String tamperedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
				("{\"sub\":\"" + USER_ID + "\",\"role\":\"ADMIN\",\"exp\":9999999999}").getBytes(StandardCharsets.UTF_8));

		assertInvalid(parts[0] + "." + tamperedPayload + "." + parts[2]);
	}

	@Test
	void 서명_없는_토큰은_TOKEN_INVALID() {
		String unsigned = Jwts.builder()
				.subject(USER_ID.toString())
				.claim("role", "USER")
				.expiration(Date.from(NOW.plus(Duration.ofMinutes(30))))
				.compact();

		assertInvalid(unsigned);
	}

	@Test
	void 형식이_깨진_문자열은_TOKEN_INVALID() {
		assertInvalid("not-a-jwt");
		assertInvalid("");
	}

	@Test
	void subject가_UUID가_아니면_TOKEN_INVALID() {
		String token = Jwts.builder()
				.subject("not-uuid")
				.claim("role", "USER")
				.expiration(Date.from(NOW.plus(Duration.ofMinutes(30))))
				.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
				.compact();

		assertInvalid(token);
	}

	@Test
	void role이_없으면_TOKEN_INVALID() {
		String token = Jwts.builder()
				.subject(USER_ID.toString())
				.expiration(Date.from(NOW.plus(Duration.ofMinutes(30))))
				.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
				.compact();

		assertInvalid(token);
	}

	private void assertInvalid(String token) {
		assertThatThrownBy(() -> providerAt(NOW).parse(token))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode").isEqualTo(ErrorCode.TOKEN_INVALID);
	}

}
