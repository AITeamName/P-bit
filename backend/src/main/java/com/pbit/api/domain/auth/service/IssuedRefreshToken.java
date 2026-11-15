package com.pbit.api.domain.auth.service;

import java.time.Instant;
import java.util.UUID;

import com.pbit.api.domain.auth.entity.ClientType;

// 발급 결과. rawToken은 응답(쿠키·body)으로만 내보내고 저장하지 않는다
public record IssuedRefreshToken(UUID userId, ClientType clientType, String rawToken, Instant expiresAt) {
}
