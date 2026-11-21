package com.pbit.api.domain.term.dto;

import java.time.Instant;

import com.pbit.api.domain.term.entity.Term;
import com.pbit.api.domain.term.entity.TermCode;

public record TermDetailResponse(
	TermCode termCode,
	String title,
	String version,
	boolean required,
	String content,
	Instant effectiveAt
) {

	public static TermDetailResponse from(Term term) {
		return new TermDetailResponse(
			term.getCode(),
			term.getTitle(),
			term.getVersion(),
			term.isRequired(),
			term.getContent(),
			term.getEffectiveAt()
		);
	}

}
