package com.pbit.api.domain.user.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// FE(JS \s)와 같은 결과가 나오도록 유니코드 공백도 거부하는지 확인한다
class UserRulesTest {

	private static final String IDEOGRAPHIC_SPACE = "　"; // 전각 공백
	private static final String NBSP = " ";

	@ParameterizedTest
	@ValueSource(strings = {IDEOGRAPHIC_SPACE + "도현", "도현" + IDEOGRAPHIC_SPACE, NBSP + "도현", "도현" + NBSP, " 도현", "도현 "})
	void 닉네임_앞뒤에_유니코드_공백이_있으면_거부한다(String nickname) {
		assertThat(matches(UserRules.NICKNAME_PATTERN, nickname)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"도현", "도 현", "Do Hyun"})
	void 닉네임_가운데_공백은_허용한다(String nickname) {
		assertThat(matches(UserRules.NICKNAME_PATTERN, nickname)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"a" + IDEOGRAPHIC_SPACE + "b@example.com", NBSP + "a@example.com", "a@example.com" + NBSP})
	void 이메일에_유니코드_공백이_있으면_거부한다(String email) {
		assertThat(matches(UserRules.EMAIL_PATTERN, email)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"P@ssw0rd" + IDEOGRAPHIC_SPACE, "P@ss" + NBSP + "w0rd", IDEOGRAPHIC_SPACE + "P@ssw0rd!"})
	void 비밀번호에_유니코드_공백이_있으면_거부한다(String password) {
		assertThat(matches(UserRules.PASSWORD_PATTERN, password)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"P@ssw" + "٠" + "rd!", "Password!" + "１"}) // 아랍 숫자 0, 전각 숫자 1만 있는 경우
	void 비밀번호_숫자는_ASCII_숫자만_인정한다(String password) {
		assertThat(matches(UserRules.PASSWORD_PATTERN, password)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"P@ssw0rd!", "abc123!@#"})
	void 규칙에_맞는_비밀번호는_통과한다(String password) {
		assertThat(matches(UserRules.PASSWORD_PATTERN, password)).isTrue();
	}

	// Bean Validation @Pattern과 같은 방식(전체 일치)으로 검사한다
	private static boolean matches(String regex, String value) {
		return Pattern.compile(regex).matcher(value).matches();
	}

}
