package com.pbit.api.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// 로그인은 형식 검증을 하지 않는다. 형식이 틀린 값도 INVALID_CREDENTIALS로 같은 응답을 줘야 하기 때문이다
public record LoginRequest(
	@Schema(description = "이메일", example = "dohyun@example.com")
	@NotBlank(message = "이메일을 입력해주세요") String email,

	@Schema(description = "비밀번호", example = "P@ssw0rd!")
	@NotBlank(message = "비밀번호를 입력해주세요") String password
) {
}
