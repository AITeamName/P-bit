package com.pbit.api.domain.user.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 가입 시점에 시행 중이던 약관 버전(term_id)별 동의 여부. 선택 약관 미동의도 agreed=false로 남긴다
@Entity
@Table(name = "user_term_agreements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTermAgreement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "term_id", nullable = false)
	private Long termId;

	@Column(nullable = false)
	private boolean agreed;

	@Column(name = "agreed_at", nullable = false)
	private Instant agreedAt;

	public UserTermAgreement(UUID userId, Long termId, boolean agreed, Instant agreedAt) {
		this.userId = userId;
		this.termId = termId;
		this.agreed = agreed;
		this.agreedAt = agreedAt;
	}

}
