package com.pbit.api.domain.auth.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pbit.api.domain.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	// 유효한 토큰만 폐기한다. 반환값 0 = 이미 폐기됐거나 만료됐거나 없음.
	// 조회 후 저장이 아니라 조건부 UPDATE 한 번이라, 같은 토큰으로 동시에 재발급해도 한 요청만 성공한다
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update RefreshToken r set r.revokedAt = :now
			where r.tokenHash = :tokenHash and r.revokedAt is null and r.expiresAt > :now
			""")
	int revokeIfActive(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

}
