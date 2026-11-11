package com.pbit.api.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 공통
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력한 내용을 다시 확인해주세요.", false),
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없어요.", false),
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없어요.", false),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청이에요.", false),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "잠시 후 다시 시도해주세요.", false),

	// 인증·회원
	REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "필수 약관에 동의해주세요.", false),
	PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않아요.", false),
	EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일이에요.", false),
	NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임이에요.", false),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호를 확인해주세요.", false),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "로그인이 만료되었어요.", false),
	TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "다시 로그인해주세요.", false),
	REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "다시 로그인해주세요.", false),

	// 약관
	TERM_NOT_FOUND(HttpStatus.NOT_FOUND, "약관을 찾을 수 없어요.", false);

	private final HttpStatus status; // HTTP 상태 응답
	private final String userMessage; // 화면에 표시할 문구
	private final boolean retryable; // 같은 요청을 그대로 재시도 해도 되는지
}
