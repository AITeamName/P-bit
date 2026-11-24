package com.pbit.api.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import com.pbit.api.domain.user.entity.Domain;
import com.pbit.api.domain.user.entity.Level;
import com.pbit.api.domain.user.entity.SelfReportedLevel;
import com.pbit.api.domain.user.entity.User;
import com.pbit.api.support.TestJwtConfig;

import jakarta.persistence.EntityManager;

// 실제 PostgreSQL(Flyway 스키마)에 저장해 컬럼 매핑과 기본값을 확인한다. 테스트마다 롤백된다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManager entityManager;

	@Test
	void 자체_가입_회원을_저장하면_기본값이_채워진다() {
		User saved = userRepository.saveAndFlush(
				User.createLocal("dohyun@example.com", "$2a$10$hash", "도현", TestJwtConfig.NOW));

		Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM users WHERE id = ?", saved.getId());
		assertThat(row.get("email")).isEqualTo("dohyun@example.com");
		assertThat(row.get("nickname")).isEqualTo("도현");
		assertThat(row.get("provider")).isEqualTo("LOCAL");
		assertThat(row.get("role")).isEqualTo("USER");
		assertThat(row.get("language")).isEqualTo("ko");
		assertThat(row.get("onboarding_completed")).isEqualTo(false);
		assertThat(row.get("deleted_at")).isNull();
	}

	@Test
	void 가입_직후에는_도메인과_레벨이_비어_있다() {
		User saved = userRepository.saveAndFlush(
				User.createLocal("dohyun@example.com", "$2a$10$hash", "도현", TestJwtConfig.NOW));

		assertThat(saved.getDomain()).isNull();
		assertThat(saved.getSelfReportedLevel()).isNull();
		assertThat(saved.getCurrentLevel()).isNull();
		assertThat(saved.getLevelMeasuredAt()).isNull();
	}

	@Test
	void DB에_저장된_도메인과_레벨을_읽는다() {
		User saved = userRepository.saveAndFlush(
				User.createLocal("dohyun@example.com", "$2a$10$hash", "도현", TestJwtConfig.NOW));
		jdbcTemplate.update("""
				UPDATE users SET domain = 'MARKETING', self_reported_level = 'INTERMEDIATE',
				current_level = 'LV2', level_measured_at = ? WHERE id = ?
				""", Timestamp.from(TestJwtConfig.NOW), saved.getId());
		// SQL로 바꾼 값을 다시 읽도록 JPA가 들고 있는 객체를 비운다
		entityManager.clear();

		User found = userRepository.findById(saved.getId()).orElseThrow();
		assertThat(found.getDomain()).isEqualTo(Domain.MARKETING);
		assertThat(found.getSelfReportedLevel()).isEqualTo(SelfReportedLevel.INTERMEDIATE);
		assertThat(found.getCurrentLevel()).isEqualTo(Level.LV2);
		assertThat(found.getLevelMeasuredAt()).isEqualTo(TestJwtConfig.NOW);
	}

	@Test
	void 이메일과_닉네임_존재_여부를_확인한다() {
		userRepository.saveAndFlush(User.createLocal("dohyun@example.com", "$2a$10$hash", "도현", TestJwtConfig.NOW));

		assertThat(userRepository.existsByEmail("dohyun@example.com")).isTrue();
		assertThat(userRepository.existsByEmail("other@example.com")).isFalse();
		assertThat(userRepository.existsByNickname("도현")).isTrue();
		assertThat(userRepository.existsByNickname("다른닉")).isFalse();
		assertThat(userRepository.findByEmail("dohyun@example.com")).isPresent();
	}

}
