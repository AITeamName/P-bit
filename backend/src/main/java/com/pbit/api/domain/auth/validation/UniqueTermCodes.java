package com.pbit.api.domain.auth.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

// 약관 동의 목록에 같은 termCode가 두 번 이상 오면 거부한다.
// 동의 기록은 법적 근거가 되는 데이터라 {SERVICE,false},{SERVICE,true} 같은 애매한 입력은 받지 않는다
@Documented
@Constraint(validatedBy = UniqueTermCodesValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueTermCodes {

	String message() default "같은 약관이 중복되어 있어요";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
