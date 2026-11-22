package com.pbit.api.domain.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbit.api.domain.auth.dto.request.LoginRequest;
import com.pbit.api.domain.auth.dto.request.SignupRequest;
import com.pbit.api.domain.auth.dto.response.AvailabilityResponse;
import com.pbit.api.domain.auth.dto.response.LoginResponse;
import com.pbit.api.domain.auth.dto.response.SignupResponse;
import com.pbit.api.domain.auth.dto.response.TokenRefreshResponse;
import com.pbit.api.domain.auth.service.LoginResult;
import com.pbit.api.domain.auth.service.LoginService;
import com.pbit.api.domain.auth.service.RefreshTokenService;
import com.pbit.api.domain.auth.service.SignupResult;
import com.pbit.api.domain.auth.service.SignupService;
import com.pbit.api.domain.auth.service.TokenRefreshResult;
import com.pbit.api.domain.auth.service.TokenRefreshService;
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

@Tag(name = "인증", description = "회원가입·로그인·토큰 재발급·로그아웃")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final SignupService signupService;
	private final LoginService loginService;
	private final TokenRefreshService tokenRefreshService;
	private final RefreshTokenService refreshTokenService;
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

	@Operation(summary = "로그인", description = "이메일 로그인 + 토큰 발급 (WEB-AUTH-01)")
	@SecurityRequirements
	@ApiResponse(responseCode = "200", description = "로그인 성공. refresh 토큰은 Set-Cookie로 발급",
		headers = @Header(name = HttpHeaders.SET_COOKIE, description = "refresh_token=...; Path=/api/v1/auth; HttpOnly; SameSite=Lax"))
	@ApiResponse(responseCode = "400", description = "VALIDATION_ERROR 이메일·비밀번호 누락",
		content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "401", description = "INVALID_CREDENTIALS 이메일·비밀번호 불일치 (원인을 구분하지 않음)",
		content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		LoginResult result = loginService.login(request);
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.create(result.refreshToken()).toString())
			.body(LoginResponse.from(result));
	}

	@Operation(summary = "토큰 재발급", description = "refresh_token 쿠키로 새 Access Token 발급 + Refresh Token 교체(rotation). "
		+ "Access Token이 만료돼도 호출할 수 있다. 같은 Refresh Token은 한 번만 쓸 수 있어 동시에 두 번 호출하면 두 번째는 401")
	@SecurityRequirements
	@ApiResponse(responseCode = "200", description = "재발급 성공. 새 refresh 토큰은 Set-Cookie로 교체",
		headers = @Header(name = HttpHeaders.SET_COOKIE, description = "refresh_token=...; Path=/api/v1/auth; HttpOnly; SameSite=Lax"))
	@ApiResponse(responseCode = "401", description = "REFRESH_TOKEN_INVALID 쿠키 없음·만료·이미 사용·탈퇴 회원 → 다시 로그인",
		content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PostMapping("/refresh")
	public ResponseEntity<TokenRefreshResponse> refresh(
		@Parameter(hidden = true) @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME, required = false) String refreshToken
	) {
		// 쿠키가 없으면 null로 들어오고, 서비스가 REFRESH_TOKEN_INVALID로 처리한다
		TokenRefreshResult result = tokenRefreshService.refresh(refreshToken);
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.create(result.refreshToken()).toString())
			.body(TokenRefreshResponse.from(result));
	}

	@Operation(summary = "로그아웃", description = "refresh_token 쿠키의 Refresh Token을 폐기하고 쿠키를 지운다 (현재 기기만). "
		+ "Access Token이 만료돼도 호출할 수 있다. 쿠키가 없거나 이미 폐기된 토큰이어도 204")
	@SecurityRequirements
	@ApiResponse(responseCode = "204", description = "로그아웃 완료. refresh_token 쿠키 삭제",
		headers = @Header(name = HttpHeaders.SET_COOKIE, description = "refresh_token=; Path=/api/v1/auth; Max-Age=0; HttpOnly; SameSite=Lax"))
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
		@Parameter(hidden = true) @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME, required = false) String refreshToken
	) {
		// 프론트는 결과와 상관없이 로그인 화면으로 가므로, 토큰 상태와 관계없이 항상 성공으로 응답한다
		refreshTokenService.revoke(refreshToken);
		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.delete().toString())
			.build();
	}

}
