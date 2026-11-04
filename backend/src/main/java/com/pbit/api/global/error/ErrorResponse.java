package com.pbit.api.global.error;

import java.util.List;

import org.slf4j.MDC;

import com.pbit.api.global.logging.RequestIdFilter;

public record ErrorResponse(Error error, Object fallback) {

	public record Error(
			String code,
			String userMessage,
			boolean retryable,
			String requestId,
			List<FieldError> fieldErrors
	) {
	}

	public record FieldError(String field, String reason) {
	}

	public static ErrorResponse of(ErrorCode errorCode) {
		return of(errorCode, List.of());
	}

	public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
		Error error = new Error(
				errorCode.name(),
				errorCode.getUserMessage(),
				errorCode.isRetryable(),
				MDC.get(RequestIdFilter.MDC_KEY),
				fieldErrors
		);
		return new ErrorResponse(error, null);
	}

}
