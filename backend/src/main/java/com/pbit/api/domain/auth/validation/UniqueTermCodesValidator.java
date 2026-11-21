package com.pbit.api.domain.auth.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.pbit.api.domain.auth.dto.request.SignupRequest.Agreement;
import com.pbit.api.domain.term.entity.TermCode;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueTermCodesValidator implements ConstraintValidator<UniqueTermCodes, List<Agreement>> {

	@Override
	public boolean isValid(List<Agreement> agreements, ConstraintValidatorContext context) {
		if (agreements == null) {
			return true; // null은 @NotNull이 처리한다
		}
		Set<TermCode> seen = new HashSet<>();
		return agreements.stream()
			.filter(Objects::nonNull)
			.map(Agreement::termCode)
			.filter(Objects::nonNull)
			.allMatch(seen::add);
	}

}
