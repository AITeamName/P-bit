package com.pbit.api.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import com.pbit.api.domain.user.entity.User;
import com.pbit.api.support.TestJwtConfig;

// 실제 PostgreSQL(Flyway 스키마)에 저장해 컬럼 매핑과 기본값을 확인한다. 테스트마다 롤백된다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

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
	void 이메일과_닉네임_존재_여부를_확인한다() {
		userRepository.saveAndFlush(User.createLocal("dohyun@example.com", "$2a$10$hash", "도현", TestJwtConfig.NOW));

		assertThat(userRepository.existsByEmail("dohyun@example.com")).isTrue();
		assertThat(userRepository.existsByEmail("other@example.com")).isFalse();
		assertThat(userRepository.existsByNickname("도현")).isTrue();
		assertThat(userRepository.existsByNickname("다른닉")).isFalse();
		assertThat(userRepository.findByEmail("dohyun@example.com")).isPresent();
	}

}
