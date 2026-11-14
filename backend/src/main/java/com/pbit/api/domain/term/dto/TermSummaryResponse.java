package com.pbit.api.domain.term.dto;

import com.pbit.api.domain.term.entity.Term;
import com.pbit.api.domain.term.entity.TermCode;

public record TermSummaryResponse(
	TermCode termCode,
	String title,
	String version,
	boolean required
) {

	public static TermSummaryResponse from(Term term) {
		return new TermSummaryResponse(term.getCode(), term.getTitle(), term.getVersion(), term.isRequired());
	}

}
