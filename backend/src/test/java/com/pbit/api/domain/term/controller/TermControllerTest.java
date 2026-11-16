package com.pbit.api.domain.term.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.pbit.api.domain.term.entity.Term;
import com.pbit.api.domain.term.entity.TermCode;
import com.pbit.api.domain.term.repository.TermRepository;
import com.pbit.api.domain.term.service.TermService;
import com.pbit.api.global.config.CorsConfig;
import com.pbit.api.global.config.SecurityConfig;
import com.pbit.api.global.error.ErrorResponseWriter;

// 웹 계층 + 서비스만 띄운다. 리포지토리는 목이라 DB 없이 실행된다.
// 모든 요청을 인증 없이 보낸다 (permitAll 확인).
@WebMvcTest(controllers = TermController.class)
@Import({TermService.class, SecurityConfig.class, CorsConfig.class, ErrorResponseWriter.class})
class TermControllerTest {

	private static final Instant EFFECTIVE_AT = Instant.parse("2026-11-01T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TermRepository termRepository;

	@Test
	void 약관_목록을_인증_없이_조회한다() throws Exception {
		List<Term> terms = List.of(
				term(TermCode.SERVICE, "서비스 이용약관", true),
				term(TermCode.PRIVACY, "개인정보 수집 및 이용", true),
				term(TermCode.MARKETING, "마케팅 정보 수신", false));
		given(termRepository.findAllCurrent(any())).willReturn(terms);

		mockMvc.perform(get("/api/v1/terms"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[0].termCode").value("SERVICE"))
				.andExpect(jsonPath("$[0].title").value("서비스 이용약관"))
				.andExpect(jsonPath("$[0].version").value("1.0"))
				.andExpect(jsonPath("$[0].required").value(true))
				.andExpect(jsonPath("$[0].content").doesNotExist())
				.andExpect(jsonPath("$[2].termCode").value("MARKETING"))
				.andExpect(jsonPath("$[2].required").value(false));
	}

	@Test
	void 약관_전문을_인증_없이_조회한다() throws Exception {
		Term privacy = term(TermCode.PRIVACY, "개인정보 수집 및 이용", true);
		given(termRepository.findFirstByCodeAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(eq(TermCode.PRIVACY), any()))
				.willReturn(Optional.of(privacy));

		mockMvc.perform(get("/api/v1/terms/PRIVACY"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.termCode").value("PRIVACY"))
				.andExpect(jsonPath("$.title").value("개인정보 수집 및 이용"))
				.andExpect(jsonPath("$.version").value("1.0"))
				.andExpect(jsonPath("$.required").value(true))
				.andExpect(jsonPath("$.content").value("## 전문"))
				.andExpect(jsonPath("$.effectiveAt").value("2026-11-01T00:00:00Z"));
	}

	@Test
	void 없는_약관_코드는_404_TERM_NOT_FOUND() throws Exception {
		mockMvc.perform(get("/api/v1/terms/UNKNOWN"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("TERM_NOT_FOUND"))
				.andExpect(jsonPath("$.error.userMessage").value("약관을 찾을 수 없어요."));
	}

	@Test
	void 소문자_약관_코드는_404_TERM_NOT_FOUND() throws Exception {
		mockMvc.perform(get("/api/v1/terms/privacy"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("TERM_NOT_FOUND"));
	}

	@Test
	void 코드는_맞지만_시행_중인_버전이_없으면_404_TERM_NOT_FOUND() throws Exception {
		given(termRepository.findFirstByCodeAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(eq(TermCode.SERVICE), any()))
				.willReturn(Optional.empty());

		mockMvc.perform(get("/api/v1/terms/SERVICE"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("TERM_NOT_FOUND"));
	}

	private Term term(TermCode code, String title, boolean required) {
		Term term = mock(Term.class);
		given(term.getCode()).willReturn(code);
		given(term.getTitle()).willReturn(title);
		given(term.getVersion()).willReturn("1.0");
		given(term.isRequired()).willReturn(required);
		given(term.getContent()).willReturn("## 전문");
		given(term.getEffectiveAt()).willReturn(EFFECTIVE_AT);
		return term;
	}

}
