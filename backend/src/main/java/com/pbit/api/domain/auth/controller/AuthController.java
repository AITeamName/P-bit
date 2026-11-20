package com.pbit.api.domain.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbit.api.domain.auth.dto.AvailabilityResponse;
import com.pbit.api.domain.auth.dto.SignupRequest;
import com.pbit.api.domain.auth.dto.SignupResponse;
import com.pbit.api.domain.auth.service.SignupResult;
import com.pbit.api.domain.auth.service.SignupService;
import com.pbit.api.domain.user.validation.UserRules;
import com.pbit.api.global.auth.RefreshTokenCookieFactory;
import com.pbit.api.global.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
	private final RefreshTokenCookieFactory refreshTokenCookieFactory;

	@Operation(summary = "회원가입", description = "이메일 자체 가입 + 약관 동의 저장 + 토큰 발급 (WEB-JOIN-01, 02)")
	@SecurityRequirements
	@ApiResponse(responseCode = "201", description = "가입 완료. refresh 토큰은 Set-Cookie로 발급",
		headers = @Header(name = HttpHeaders.SET_COOKIE, description = "refresh_token=...; Path=/api/v1/auth; HttpOnly; SameSite=Lax"))
	@ApiResponse(responseCode = "400", description = "VALIDATION_ERROR, PASSWORD_MISMATCH, REQUIRED_TERMS_NOT_AGREED",
		content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "409", description = "EMAIL_DUPLICATED, NICKNAME_DUPLICATED",
		content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PostMapping("/signup")
	public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
		SignupResult result = signupService.signup(request);
		return ResponseEntity.status(HttpStatus.CREATED)
			.header(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.create(result.refreshToken()).toString())
			.body(SignupResponse.from(result));
	}

	@Operation(summary = "이메일 중복 확인", description = "회원가입 이메일 실시간 검사 (WEB-JOIN-02-E). 대소문자는 구분하지 않는다")
	@SecurityRequirements
	@ApiResponse(responseCode = "200", description = "확인 결과")
	@ApiResponse(responseCode = "400", description = "VALIDATION_ERROR 이메일 형식 오류",
		content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@GetMapping("/check-email")
	public AvailabilityResponse checkEmail(
		@Parameter(description = "확인할 이메일", example = "dohyun@example.com")
		@RequestParam @NotBlank(message = UserRules.EMAIL_MESSAGE) @Size(max = UserRules.EMAIL_MAX, message = UserRules.EMAIL_MESSAGE)
		@Pattern(regexp = UserRules.EMAIL_PATTERN, message = UserRules.EMAIL_MESSAGE) String email
	) {
		return new AvailabilityResponse(signupService.isEmailAvailable(email));
	}

	@Operation(summary = "닉네임 중복 확인", description = "회원가입 닉네임 실시간 검사 (WEB-JOIN-02-E, 02B)")
	@SecurityRequirements
	@ApiResponse(responseCode = "200", description = "확인 결과")
	@ApiResponse(responseCode = "400", description = "VALIDATION_ERROR 길이 위반·앞뒤 공백",
		content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@GetMapping("/check-nickname")
	public AvailabilityResponse checkNickname(
		@Parameter(description = "확인할 닉네임 (2~10자)", example = "도현")
		@RequestParam @Size(min = UserRules.NICKNAME_MIN, max = UserRules.NICKNAME_MAX, message = UserRules.NICKNAME_MESSAGE)
		@Pattern(regexp = UserRules.NICKNAME_PATTERN, message = UserRules.NICKNAME_MESSAGE) String nickname
	) {
		return new AvailabilityResponse(signupService.isNicknameAvailable(nickname));
	}

}
