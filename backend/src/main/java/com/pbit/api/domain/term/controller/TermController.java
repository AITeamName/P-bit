package com.pbit.api.domain.term.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbit.api.domain.term.dto.TermDetailResponse;
import com.pbit.api.domain.term.dto.TermSummaryResponse;
import com.pbit.api.domain.term.service.TermService;
import com.pbit.api.global.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "약관", description = "회원가입 약관 조회 (인증 불필요)")
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermController {

	private final TermService termService;

	@Operation(summary = "약관 목록 조회", description = "현재 시행 중인 약관 목록. 가입 화면 체크 항목 (WEB-JOIN-01)")
	@ApiResponse(responseCode = "200", description = "약관 목록")
	@GetMapping
	public List<TermSummaryResponse> getTerms() {
		return termService.getCurrentTerms();
	}

	@Operation(summary = "약관 상세 조회", description = "약관 전문 (WEB-JOIN-01-E 모달). content는 마크다운")
	@ApiResponse(responseCode = "200", description = "약관 상세")
	@ApiResponse(responseCode = "404", description = "TERM_NOT_FOUND",
		content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@GetMapping("/{termCode}")
	public TermDetailResponse getTerm(
		@Parameter(description = "약관 코드", example = "PRIVACY",
			schema = @Schema(allowableValues = {"SERVICE", "PRIVACY", "MARKETING"}))
		@PathVariable String termCode
	) {
		return termService.getCurrentTerm(termCode);
	}

}
