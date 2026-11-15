package com.pbit.api.domain.auth.dto;

import java.util.List;

import com.pbit.api.domain.term.entity.TermCode;
import com.pbit.api.domain.user.validation.UserRules;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
	@Schema(description = "이메일. 대소문자 구분 없음", example = "dohyun@example.com")
	@NotBlank(message = UserRules.EMAIL_MESSAGE) @Size(max = UserRules.EMAIL_MAX, message = UserRules.EMAIL_MESSAGE)
	@Pattern(regexp = UserRules.EMAIL_PATTERN, message = UserRules.EMAIL_MESSAGE)
	String email,

	@Schema(description = "8~20자, 영문·숫자·특수문자 각 1개 이상", example = "P@ssw0rd!")
	@NotNull(message = UserRules.PASSWORD_MESSAGE) @Pattern(regexp = UserRules.PASSWORD_PATTERN, message = UserRules.PASSWORD_MESSAGE)
	String password,

	@Schema(description = "password와 동일", example = "P@ssw0rd!")
	@NotNull
	String passwordConfirm,

	@Schema(description = "2~10자, 앞뒤 공백 불가", example = "도현")
	@NotNull(message = UserRules.NICKNAME_MESSAGE)
	@Size(min = UserRules.NICKNAME_MIN, max = UserRules.NICKNAME_MAX, message = UserRules.NICKNAME_MESSAGE)
	@Pattern(regexp = UserRules.NICKNAME_PATTERN, message = UserRules.NICKNAME_MESSAGE)
	String nickname,

	@Schema(description = "약관 동의 목록. 빠진 약관은 미동의로 처리한다")
	@NotNull
	List<@NotNull @Valid Agreement> agreements
) {

	public record Agreement(
		@Schema(example = "SERVICE") @NotNull TermCode termCode,
		@Schema(example = "true") @NotNull Boolean agreed
	) {
	}

}
