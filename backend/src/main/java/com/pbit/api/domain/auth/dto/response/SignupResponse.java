package com.pbit.api.domain.auth.dto.response;

import java.util.UUID;

import com.pbit.api.domain.auth.service.SignupResult;

import io.swagger.v3.oas.annotations.media.Schema;

// refresh 토큰은 본문에 넣지 않고 Set-Cookie(refresh_token)로만 보낸다
public record SignupResponse(
	@Schema(description = "회원 ID") UUID userId,
	@Schema(description = "Access Token (JWT)") String accessToken,
	@Schema(description = "Access Token 만료(초)", example = "1800") long expiresIn,
	@Schema(description = "false면 온보딩(WEB-ONB-00)으로 이동", example = "false") boolean onboardingCompleted
) {

	public static SignupResponse from(SignupResult result) {
		return new SignupResponse(result.userId(), result.accessToken(), result.expiresIn(), result.onboardingCompleted());
	}

}
