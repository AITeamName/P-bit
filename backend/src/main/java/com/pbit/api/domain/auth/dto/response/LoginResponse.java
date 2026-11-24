package com.pbit.api.domain.auth.dto.response;

import java.util.UUID;

import com.pbit.api.domain.auth.service.LoginResult;

import io.swagger.v3.oas.annotations.media.Schema;

// refresh 토큰은 본문에 넣지 않고 Set-Cookie(refresh_token)로만 보낸다
public record LoginResponse(
	@Schema(description = "회원 ID") UUID userId,
	@Schema(description = "Access Token (JWT)") String accessToken,
	@Schema(description = "Access Token 만료(초)", example = "1800") long expiresIn,
	@Schema(description = "false면 온보딩(WEB-ONB-00), true면 대시보드로 이동", example = "true") boolean onboardingCompleted
) {

	public static LoginResponse from(LoginResult result) {
		return new LoginResponse(result.userId(), result.accessToken(), result.expiresIn(), result.onboardingCompleted());
	}

}
