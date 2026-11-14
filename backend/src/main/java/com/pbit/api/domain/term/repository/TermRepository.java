package com.pbit.api.domain.term.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pbit.api.domain.term.entity.Term;
import com.pbit.api.domain.term.entity.TermCode;

public interface TermRepository extends JpaRepository<Term, Long> {

	/**
	 * 코드별로 시행 중인 최신 버전 약관만 조회한다.
	 */
	@Query("""
		SELECT t FROM Term t
		WHERE t.effectiveAt = (
			SELECT MAX(t2.effectiveAt) FROM Term t2
			WHERE t2.code = t.code AND t2.effectiveAt <= :now
		)
		ORDER BY t.id
		""")
	List<Term> findAllCurrent(@Param("now") Instant now);

	/**
	 * 특정 코드의 시행 중인 최신 버전 약관을 조회한다.
	 */
	Optional<Term> findFirstByCodeAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(TermCode code, Instant now);

}
