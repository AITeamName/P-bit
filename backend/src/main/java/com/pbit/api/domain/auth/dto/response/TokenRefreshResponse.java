package com.pbit.api.domain.auth.dto.response;

import com.pbit.api.domain.auth.service.TokenRefreshResult;

import io.swagger.v3.oas.annotations.media.Schema;

// 새 refresh 토큰은 본문에 넣지 않고 Set-Cookie(refresh_token)로만 보낸다
public record TokenRefreshResponse(
	@Schema(description = "새 Access Token (JWT)") String accessToken,
	@Schema(description = "Access Token 만료(초)", example = "1800") long expiresIn
) {

	public static TokenRefreshResponse from(TokenRefreshResult result) {
		return new TokenRefreshResponse(result.accessToken(), result.expiresIn());
	}

}
