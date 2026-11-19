package com.pbit.api.domain.term.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbit.api.domain.term.dto.TermDetailResponse;
import com.pbit.api.domain.term.dto.TermSummaryResponse;
import com.pbit.api.domain.term.entity.TermCode;
import com.pbit.api.domain.term.repository.TermRepository;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermService {

	private final TermRepository termRepository;

	/**
	 * 현재 시행 중인 약관 목록 (코드별 최신 버전 1개씩).
	 */
	public List<TermSummaryResponse> getCurrentTerms() {
		return termRepository.findAllCurrent(Instant.now()).stream()
			.map(TermSummaryResponse::from)
			.toList();
	}

	/**
	 * 약관 전문 조회. 없는 코드(대소문자 다름 포함)면 TERM_NOT_FOUND.
	 */
	public TermDetailResponse getCurrentTerm(String termCode) {
		TermCode code = parseCode(termCode);
		return termRepository.findFirstByCodeAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(code, Instant.now())
			.map(TermDetailResponse::from)
			.orElseThrow(() -> new BusinessException(ErrorCode.TERM_NOT_FOUND));
	}

	private TermCode parseCode(String termCode) {
		return Arrays.stream(TermCode.values())
			.filter(code -> code.name().equals(termCode))
			.findFirst()
			.orElseThrow(() -> new BusinessException(ErrorCode.TERM_NOT_FOUND));
	}

}
