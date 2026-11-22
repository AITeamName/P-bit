package com.pbit.api.global.auth;

import java.util.UUID;

// Access Token에서 꺼낸 로그인 사용자. SecurityContext의 principal로 쓴다
public record AuthUser(UUID userId, String role) {
}
