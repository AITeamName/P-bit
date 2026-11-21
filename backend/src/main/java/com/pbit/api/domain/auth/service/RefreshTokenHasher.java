package com.pbit.api.domain.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// Refresh Token 원문 → SHA-256 hex(64자). DB에는 이 값만 저장한다
final class RefreshTokenHasher {

	private RefreshTokenHasher() {
	}

	static String hash(String rawToken) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256을 지원하지 않는 JVM", e);
		}
	}

}
