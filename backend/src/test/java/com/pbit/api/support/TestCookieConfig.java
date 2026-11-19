package com.pbit.api.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.pbit.api.global.auth.RefreshCookieProperties;
import com.pbit.api.global.auth.RefreshTokenCookieFactory;

// @WebMvcTest에서 refresh 쿠키를 발급하는 컨트롤러를 띄울 때 사용. 운영 설정처럼 Secure를 켠다
@TestConfiguration
public class TestCookieConfig {

	@Bean
	RefreshTokenCookieFactory refreshTokenCookieFactory() {
		return new RefreshTokenCookieFactory(TestJwtConfig.PROPERTIES, new RefreshCookieProperties(true));
	}

}
