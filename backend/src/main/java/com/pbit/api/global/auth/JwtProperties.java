package com.pbit.api.global.auth;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// app.jwt.* 설정. 값이 비었거나 키가 짧으면 서버가 뜨지 않는다
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
		@NotBlank String secret,
		@NotNull Duration accessTokenTtl,
		@NotNull Duration refreshTokenTtl
) {

	private static final int MIN_SECRET_BYTES = 32; // HS256 최소 키 길이

	@AssertTrue(message = "JWT_SECRET은 32바이트 이상이어야 합니다")
	public boolean isSecretLongEnough() {
		return secret == null || secret.getBytes(StandardCharsets.UTF_8).length >= MIN_SECRET_BYTES;
	}

}
