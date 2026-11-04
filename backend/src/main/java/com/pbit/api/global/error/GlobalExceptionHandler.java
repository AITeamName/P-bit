package com.pbit.api.global.error;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.pbit.api.global.error.ErrorResponse.FieldError;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
		return toResponse(e.getErrorCode(), List.of());
	}

	// @RequestBody @Valid 검증 실패
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException e) {
		List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
				.map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
				.toList();
		return toResponse(ErrorCode.VALIDATION_ERROR, fieldErrors);
	}

	// @RequestParam 등 메서드 파라미터 검증 실패
	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ErrorResponse> handleParameterValidation(HandlerMethodValidationException e) {
		List<FieldError> fieldErrors = e.getParameterValidationResults().stream()
				.flatMap(result -> result.getResolvableErrors().stream()
						.map(error -> new FieldError(result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
				.toList();
		return toResponse(ErrorCode.VALIDATION_ERROR, fieldErrors);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException e) {
		return toResponse(ErrorCode.VALIDATION_ERROR, List.of(new FieldError(e.getParameterName(), "required")));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		return toResponse(ErrorCode.VALIDATION_ERROR, List.of(new FieldError(e.getName(), "invalid type")));
	}

	// JSON 형식 오류, 본문 누락
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
		return toResponse(ErrorCode.VALIDATION_ERROR, List.of());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException e) {
		return toResponse(ErrorCode.NOT_FOUND, List.of());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
		return toResponse(ErrorCode.METHOD_NOT_ALLOWED, List.of());
	}

	// 예상하지 못한 예외: 원인은 로그에만 남기고 응답에는 넣지 않는다
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
		log.error("Unhandled exception", e);
		return toResponse(ErrorCode.INTERNAL_ERROR, List.of());
	}

	private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode, List<FieldError> fieldErrors) {
		return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.of(errorCode, fieldErrors));
	}

}
