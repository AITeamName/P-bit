package com.pbit.api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

// Swagger UI 우측 상단 Authorize 버튼에 Access Token을 넣으면 모든 요청에 Authorization: Bearer 헤더가 붙는다.
// 인증이 필요 없는 API(약관 등)는 토큰 없이도 호출된다.
@Configuration
public class OpenApiConfig {

	private static final String BEARER_AUTH = "bearerAuth";

	@Bean
	OpenAPI openApi() {
		return new OpenAPI()
				.info(new Info().title("P-bit API").version("v1"))
				.components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.description("POST /auth/login 등으로 받은 accessToken (30분)")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
	}

}
