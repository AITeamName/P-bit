package com.pbit.api.domain.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbit.api.domain.auth.dto.SignupRequest;
import com.pbit.api.domain.auth.entity.ClientType;
import com.pbit.api.domain.term.entity.Term;
import com.pbit.api.domain.term.entity.TermCode;
import com.pbit.api.domain.term.repository.TermRepository;
import com.pbit.api.domain.user.entity.User;
import com.pbit.api.domain.user.entity.UserTermAgreement;
import com.pbit.api.domain.user.repository.UserRepository;
import com.pbit.api.domain.user.repository.UserTermAgreementRepository;
import com.pbit.api.domain.user.validation.UserRules;
import com.pbit.api.global.auth.JwtTokenProvider;
import com.pbit.api.global.error.BusinessException;
import com.pbit.api.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupService {

	// V1__init.sql의 UNIQUE 제약 이름 (PostgreSQL 기본 이름)
	private static final String EMAIL_UNIQUE = "users_email_key";
	private static final String NICKNAME_UNIQUE = "users_nickname_key";

	private final UserRepository userRepository;
	private final UserTermAgreementRepository userTermAgreementRepository;
	private final TermRepository termRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final Clock clock;

	@Transactional(readOnly = true)
	public boolean isEmailAvailable(String email) {
		return !userRepository.existsByEmail(UserRules.normalizeEmail(email));
	}

	@Transactional(readOnly = true)
	public boolean isNicknameAvailable(String nickname) {
		return !userRepository.existsByNickname(nickname);
	}

	// 형식 검증(@Valid)은 컨트롤러에서 끝난 상태로 들어온다.
	// 검사 순서: 비밀번호 확인 → 필수 약관 → 이메일 중복 → 닉네임 중복
	@Transactional
	public SignupResult signup(SignupRequest request) {
		if (!request.password().equals(request.passwordConfirm())) {
			throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
		}

		Instant now = clock.instant();
		List<Term> currentTerms = termRepository.findAllCurrent(now);
		Set<TermCode> agreedCodes = request.agreements().stream()
			.filter(SignupRequest.Agreement::agreed)
			.map(SignupRequest.Agreement::termCode)
			.collect(Collectors.toSet());
		boolean allRequiredAgreed = currentTerms.stream()
			.filter(Term::isRequired)
			.allMatch(term -> agreedCodes.contains(term.getCode()));
		if (!allRequiredAgreed) {
			throw new BusinessException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
		}

		String email = UserRules.normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
		}
		if (userRepository.existsByNickname(request.nickname())) {
			throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
		}

		User user = saveUser(User.createLocal(email, passwordEncoder.encode(request.password()), request.nickname(), now));

		userTermAgreementRepository.saveAll(currentTerms.stream()
			.map(term -> new UserTermAgreement(user.getId(), term.getId(), agreedCodes.contains(term.getCode()), now))
			.toList());

		String accessToken = jwtTokenProvider.issueAccessToken(user.getId(), user.getRole().name());
		IssuedRefreshToken refreshToken = refreshTokenService.issue(user.getId(), ClientType.WEB);
		return new SignupResult(user.getId(), accessToken, jwtTokenProvider.accessTokenExpiresInSeconds(),
			user.isOnboardingCompleted(), refreshToken.rawToken());
	}

	// 중복 확인과 저장 사이에 같은 이메일·닉네임으로 동시에 가입하면 DB UNIQUE에서 걸린다.
	// 이때도 500이 아니라 중복 에러 코드로 응답한다
	private User saveUser(User user) {
		try {
			return userRepository.saveAndFlush(user);
		} catch (DataIntegrityViolationException e) {
			String message = String.valueOf(e.getMostSpecificCause().getMessage());
			if (message.contains(EMAIL_UNIQUE)) {
				throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
			}
			if (message.contains(NICKNAME_UNIQUE)) {
				throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
			}
			throw e;
		}
	}

}
