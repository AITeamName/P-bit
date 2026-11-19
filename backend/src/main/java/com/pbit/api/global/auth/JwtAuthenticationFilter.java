package com.pbit.api.global.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// Authorization: Bearer <token> 을 검증해 SecurityContext에 로그인 사용자를 넣는다.
// 토큰이 잘못돼도 여기서 바로 401을 쓰지 않는다. 실패 사유만 요청에 남기고 다음 필터로 넘긴다.
// → 인증이 필요한 API만 EntryPoint에서 그 사유(TOKEN_EXPIRED / TOKEN_INVALID)로 401 응답하고,
//   permitAll API(약관, 토큰 재발급 등)는 만료된 토큰이 함께 와도 그대로 통과한다.
// 빈으로 등록하지 않는다(서블릿 필터로 중복 등록 방지). SecurityConfig에서 직접 생성한다.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	public static final String AUTH_ERROR_ATTRIBUTE = JwtAuthenticationFilter.class.getName() + ".AUTH_ERROR";
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null) {
			authenticate(request, header);
		}
		filterChain.doFilter(request, response);
	}

	private void authenticate(HttpServletRequest request, String header) {
		if (!header.startsWith(BEARER_PREFIX)) {
			request.setAttribute(AUTH_ERROR_ATTRIBUTE, ErrorCode.TOKEN_INVALID);
			return;
		}
		try {
			AuthUser user = jwtTokenProvider.parse(header.substring(BEARER_PREFIX.length()).trim());
			var authentication = new UsernamePasswordAuthenticationToken(
					user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.role())));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (BusinessException e) {
			SecurityContextHolder.clearContext();
			request.setAttribute(AUTH_ERROR_ATTRIBUTE, e.getErrorCode());
		}
	}

}
