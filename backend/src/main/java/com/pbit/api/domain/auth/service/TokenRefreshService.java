package com.pbit.api.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbit.api.domain.user.entity.User;
import com.pbit.api.domain.user.repository.UserRepository;
import com.pbit.api.global.auth.JwtTokenProvider;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {

	private final RefreshTokenService refreshTokenService;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	// Refresh Token을 교체(rotation)하고 새 Access Token을 발급한다.
	// 토큰 없음·만료·이미 폐기 → REFRESH_TOKEN_INVALID (rotate에서 판단)
	// 회원이 없거나 탈퇴 → REFRESH_TOKEN_INVALID. 같은 트랜잭션이라 방금 한 교체도 함께 되돌려진다
	@Transactional
	public TokenRefreshResult refresh(String rawRefreshToken) {
		IssuedRefreshToken rotated = refreshTokenService.rotate(rawRefreshToken);

		User user = userRepository.findById(rotated.userId())
			.filter(found -> !found.isDeleted())
			.orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

		String accessToken = jwtTokenProvider.issueAccessToken(user.getId(), user.getRole().name());
		return new TokenRefreshResult(accessToken, jwtTokenProvider.accessTokenExpiresInSeconds(), rotated.rawToken());
	}

}
