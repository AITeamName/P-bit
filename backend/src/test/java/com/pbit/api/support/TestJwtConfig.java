package com.pbit.api.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.pbit.api.global.auth.JwtProperties;
import com.pbit.api.global.auth.JwtTokenProvider;

// @WebMvcTest에서 SecurityConfig를 띄울 때 필요한 JwtTokenProvider.
// 시각을 NOW로 고정해 만료 여부를 테스트에서 제어한다.
@TestConfiguration
public class TestJwtConfig {

	public static final String SECRET = "test-only-secret-key-at-least-32-bytes-long!!";
	public static final Instant NOW = Instant.parse("2026-11-15T00:00:00Z");
	public static final JwtProperties PROPERTIES = new JwtProperties(SECRET, Duration.ofMinutes(30), Duration.ofDays(14));

	public static JwtTokenProvider providerAt(Instant instant) {
		return new JwtTokenProvider(PROPERTIES, Clock.fixed(instant, ZoneOffset.UTC));
	}

	@Bean
	JwtTokenProvider jwtTokenProvider() {
		return providerAt(NOW);
	}

}
