package com.pbit.api.domain.user.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true)
	private String email;

	// BCrypt 해시. Google 가입자는 null
	@Column(name = "password_hash", length = 100)
	private String passwordHash;

	@Column(nullable = false, unique = true, length = 20)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private AuthProvider provider;

	@Column(name = "provider_id", length = 100)
	private String providerId;

	// 아래 네 값은 온보딩·진단 전에는 비어 있다. 값을 바꾸는 기능은 온보딩·진단 이슈에서 만든다
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Domain domain;

	@Enumerated(EnumType.STRING)
	@Column(name = "self_reported_level", length = 20)
	private SelfReportedLevel selfReportedLevel;

	// 진단 모드에서만 바뀐다 (연습·점검 결과로는 바꾸지 않는다)
	@Enumerated(EnumType.STRING)
	@Column(name = "current_level", length = 5)
	private Level currentLevel;

	@Column(name = "level_measured_at")
	private Instant levelMeasuredAt;

	@Column(name = "onboarding_completed", nullable = false)
	private boolean onboardingCompleted;

	@Column(nullable = false, length = 5)
	private String language;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private UserRole role;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	private User(String email, String passwordHash, String nickname, AuthProvider provider, Instant now) {
		this.email = email;
		this.passwordHash = passwordHash;
		this.nickname = nickname;
		this.provider = provider;
		this.onboardingCompleted = false;
		this.language = "ko";
		this.role = UserRole.USER;
		this.createdAt = now;
		this.updatedAt = now;
	}

	// 이메일 자체 가입. email·nickname은 정규화된 값을 받는다
	public static User createLocal(String email, String passwordHash, String nickname, Instant now) {
		return new User(email, passwordHash, nickname, AuthProvider.LOCAL, now);
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

}
