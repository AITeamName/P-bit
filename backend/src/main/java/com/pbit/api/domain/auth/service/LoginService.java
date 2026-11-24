package com.pbit.api.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbit.api.domain.auth.dto.request.LoginRequest;
import com.pbit.api.domain.auth.entity.ClientType;
import com.pbit.api.domain.user.entity.User;
import com.pbit.api.domain.user.repository.UserRepository;
import com.pbit.api.domain.user.validation.UserRules;
import com.pbit.api.global.auth.JwtTokenProvider;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;

	// 웹 이메일 로그인. 실패 사유를 구분하지 않는다 (연동 기록 3.3).
	// 이메일 없음 · 비밀번호 불일치 · 탈퇴 회원 · 비밀번호 없는 소셜 가입 회원 → 모두 INVALID_CREDENTIALS
	@Transactional
	public LoginResult login(LoginRequest request) {
		User user = userRepository.findByEmail(UserRules.normalizeEmail(request.email()))
			.filter(found -> !found.isDeleted())
			.filter(found -> found.getPasswordHash() != null)
			.filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		String accessToken = jwtTokenProvider.issueAccessToken(user.getId(), user.getRole().name());
		IssuedRefreshToken refreshToken = refreshTokenService.issue(user.getId(), ClientType.WEB);
		return new LoginResult(user.getId(), accessToken, jwtTokenProvider.accessTokenExpiresInSeconds(),
			user.isOnboardingCompleted(), refreshToken.rawToken());
	}

}
