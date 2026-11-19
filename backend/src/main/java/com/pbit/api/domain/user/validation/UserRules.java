package com.pbit.api.domain.user.validation;

import java.util.Locale;

// 회원 입력 규칙. 중복 확인 API와 회원가입 API가 같은 규칙을 쓴다 (FE와 동일, 연동 기록 3.2)
public final class UserRules {

	// (?U): \s·\S가 ASCII 공백뿐 아니라 전각 공백(U+3000)·NBSP 등 유니코드 공백도 인식하게 한다 (JS의 \s와 같은 결과).
	// (?U)를 켜면 \d도 유니코드 숫자가 되므로 숫자는 [0-9]로 쓴다.

	// 공백 없이 local@domain.tld
	public static final String EMAIL_PATTERN = "(?U)^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
	public static final int EMAIL_MAX = 255;

	// 8~20자, 영문·숫자·특수문자 각 1개 이상, 공백 불가
	public static final String PASSWORD_PATTERN = "(?U)^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s])\\S{8,20}$";

	// 2~10자, 앞뒤 공백 불가
	public static final String NICKNAME_PATTERN = "(?U)^\\S(.*\\S)?$";
	public static final int NICKNAME_MIN = 2;
	public static final int NICKNAME_MAX = 10;

	// fieldErrors[].reason 문구 (연동 기록 3.2 예시, 디자인 확정 전)
	public static final String EMAIL_MESSAGE = "올바른 이메일 형식이 아니에요";
	public static final String PASSWORD_MESSAGE = "8~20자 영문, 숫자, 특수문자를 포함해주세요";
	public static final String NICKNAME_MESSAGE = "2~10자로 입력해주세요";

	private UserRules() {
	}

	// 대소문자만 다른 이메일은 같은 이메일로 본다. 저장·비교 모두 이 값을 쓴다
	public static String normalizeEmail(String email) {
		return email.toLowerCase(Locale.ROOT);
	}

}
