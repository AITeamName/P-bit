package com.pbit.api.domain.auth.entity;

// 토큰을 발급받은 클라이언트. 웹은 쿠키, 익스텐션은 body로 refresh 토큰을 주고받는다
public enum ClientType {
	WEB,
	EXTENSION
}
