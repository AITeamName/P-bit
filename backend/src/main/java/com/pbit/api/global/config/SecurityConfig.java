package com.pbit.api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.pbit.api.global.auth.JwtAuthenticationFilter;
import com.pbit.api.global.auth.JwtTokenProvider;
import com.pbit.api.global.error.ErrorCode;
import com.pbit.api.global.error.ErrorResponseWriter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final ErrorResponseWriter errorResponseWriter;
	private final JwtTokenProvider jwtTokenProvider;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health", "/error").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/terms", "/api/v1/terms/*").permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling(exception -> exception
						// 토큰 없음 → TOKEN_INVALID, 토큰 검증 실패 → 필터가 남긴 사유
						.authenticationEntryPoint((request, response, e) -> errorResponseWriter.write(response, authErrorOf(request)))
						.accessDeniedHandler((request, response, e) -> errorResponseWriter.write(response, ErrorCode.FORBIDDEN)))
				.build();
	}

	private static ErrorCode authErrorOf(HttpServletRequest request) {
		Object error = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);
		return error instanceof ErrorCode code ? code : ErrorCode.TOKEN_INVALID;
	}

}
