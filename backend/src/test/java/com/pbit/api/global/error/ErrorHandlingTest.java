package com.pbit.api.global.error;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pbit.api.global.config.CorsConfig;
import com.pbit.api.global.config.SecurityConfig;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 웹 계층만 띄우는 테스트라 DB 없이 실행된다
@WebMvcTest(controllers = ErrorHandlingTest.TestErrorController.class)
@Import({ErrorHandlingTest.TestErrorController.class, SecurityConfig.class, CorsConfig.class, ErrorResponseWriter.class})
class ErrorHandlingTest {

	private static final String FRONT_ORIGIN = "http://localhost:5173";

	@Autowired
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	void 비즈니스_예외는_ErrorCode대로_응답한다() throws Exception {
		mockMvc.perform(get("/test-errors/business"))
				.andExpect(status().isConflict())
				.andExpect(header().exists("X-Request-Id"))
				.andExpect(jsonPath("$.error.code").value("EMAIL_DUPLICATED"))
				.andExpect(jsonPath("$.error.userMessage").value("이미 사용 중인 이메일이에요."))
				.andExpect(jsonPath("$.error.retryable").value(false))
				.andExpect(jsonPath("$.error.requestId").value(notNullValue()))
				.andExpect(jsonPath("$.error.fieldErrors", hasSize(0)))
				.andExpect(jsonPath("$.error.message").doesNotExist())
				.andExpect(jsonPath("$.fallback").value(nullValue()));
	}

	@Test
	@WithMockUser
	void 요청_본문_검증_실패는_fieldErrors로_응답한다() throws Exception {
		mockMvc.perform(post("/test-errors/validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\": \"not-email\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.error.fieldErrors[0].field").value("email"));
	}

	@Test
	@WithMockUser
	void 예상하지_못한_예외는_원인을_숨기고_500으로_응답한다() throws Exception {
		mockMvc.perform(get("/test-errors/unexpected"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
				.andExpect(jsonPath("$.error.userMessage").value("잠시 후 다시 시도해주세요."));
	}

	@Test
	void 인증_없이_호출하면_401_에러_형식으로_응답한다() throws Exception {
		mockMvc.perform(get("/test-errors/business"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().exists("X-Request-Id"))
				.andExpect(jsonPath("$.error.code").value("TOKEN_INVALID"))
				.andExpect(jsonPath("$.error.requestId").value(notNullValue()));
	}

	@Test
	void 허용된_프론트_주소의_preflight는_통과한다() throws Exception {
		mockMvc.perform(options("/test-errors/business")
						.header("Origin", FRONT_ORIGIN)
						.header("Access-Control-Request-Method", "POST")
						.header("Access-Control-Request-Headers", "Authorization, Content-Type"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", FRONT_ORIGIN))
				.andExpect(header().string("Access-Control-Allow-Credentials", "true"));
	}

	@Test
	void 허용되지_않은_주소의_preflight는_거절한다() throws Exception {
		mockMvc.perform(options("/test-errors/business")
						.header("Origin", "http://evil.example.com")
						.header("Access-Control-Request-Method", "POST"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
	}

	@RestController
	static class TestErrorController {

		@GetMapping("/test-errors/business")
		void business() {
			throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
		}

		@PostMapping("/test-errors/validation")
		void validation(@Valid @RequestBody TestRequest request) {
		}

		@GetMapping("/test-errors/unexpected")
		void unexpected() {
			throw new IllegalStateException("내부 원인");
		}

	}

	record TestRequest(@NotBlank @Email String email) {
	}

}
