package com.pbit.api.domain.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbit.api.domain.auth.dto.AvailabilityResponse;
import com.pbit.api.domain.auth.service.SignupService;
import com.pbit.api.domain.user.validation.UserRules;
import com.pbit.api.global.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@Tag(name = "인증", description = "회원가입·로그인")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final SignupService signupService;

	@Operation(summary = "이메일 중복 확인", description = "회원가입 이메일 실시간 검사 (WEB-JOIN-02-E). 대소문자는 구분하지 않는다")
	@ApiResponse(responseCode = "200", description = "확인 결과")
	@ApiResponse(responseCode = "400", description = "VALIDATION_ERROR 이메일 형식 오류",
		content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@GetMapping("/check-email")
	public AvailabilityResponse checkEmail(
		@Parameter(description = "확인할 이메일", example = "dohyun@example.com")
		@RequestParam @NotBlank @Size(max = UserRules.EMAIL_MAX) @Pattern(regexp = UserRules.EMAIL_PATTERN) String email
	) {
		return new AvailabilityResponse(signupService.isEmailAvailable(email));
	}

	@Operation(summary = "닉네임 중복 확인", description = "회원가입 닉네임 실시간 검사 (WEB-JOIN-02-E, 02B)")
	@ApiResponse(responseCode = "200", description = "확인 결과")
	@ApiResponse(responseCode = "400", description = "VALIDATION_ERROR 길이 위반·앞뒤 공백",
		content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@GetMapping("/check-nickname")
	public AvailabilityResponse checkNickname(
		@Parameter(description = "확인할 닉네임 (2~10자)", example = "도현")
		@RequestParam @Size(min = UserRules.NICKNAME_MIN, max = UserRules.NICKNAME_MAX)
		@Pattern(regexp = UserRules.NICKNAME_PATTERN) String nickname
	) {
		return new AvailabilityResponse(signupService.isNicknameAvailable(nickname));
	}

}
