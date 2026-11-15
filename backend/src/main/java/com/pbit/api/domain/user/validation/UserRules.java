package com.pbit.api.domain.user.validation;

import java.util.Locale;

// 회원 입력 규칙. 중복 확인 API와 회원가입 API가 같은 규칙을 쓴다 (FE와 동일, 연동 기록 3.2)
public final class UserRules {

	// 공백 없이 local@domain.tld
	public static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
	public static final int EMAIL_MAX = 255;

	// 8~20자, 영문·숫자·특수문자 각 1개 이상, 공백 불가
	public static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,20}$";

	// 2~10자, 앞뒤 공백 불가
	public static final String NICKNAME_PATTERN = "^\\S(.*\\S)?$";
	public static final int NICKNAME_MIN = 2;
	public static final int NICKNAME_MAX = 10;

	private UserRules() {
	}

	// 대소문자만 다른 이메일은 같은 이메일로 본다. 저장·비교 모두 이 값을 쓴다
	public static String normalizeEmail(String email) {
		return email.toLowerCase(Locale.ROOT);
	}

}
