package com.pbit.api.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbit.api.domain.user.repository.UserRepository;
import com.pbit.api.domain.user.validation.UserRules;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public boolean isEmailAvailable(String email) {
		return !userRepository.existsByEmail(UserRules.normalizeEmail(email));
	}

	@Transactional(readOnly = true)
	public boolean isNicknameAvailable(String nickname) {
		return !userRepository.existsByNickname(nickname);
	}

}
