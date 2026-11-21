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

// 온보딩·레벨 컬럼(domain, self_reported_level, current_level 등)은 해당 기능 이슈에서 매핑한다
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
