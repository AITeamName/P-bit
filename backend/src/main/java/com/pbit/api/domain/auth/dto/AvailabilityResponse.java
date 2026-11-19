package com.pbit.api.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AvailabilityResponse(
	@Schema(description = "사용 가능 여부. true면 가입 가능", example = "true")
	boolean available
) {
}
