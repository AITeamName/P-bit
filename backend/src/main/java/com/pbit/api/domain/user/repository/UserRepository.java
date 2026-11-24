package com.pbit.api.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pbit.api.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

	// 탈퇴 회원도 포함한다 (DB UNIQUE가 탈퇴 여부와 무관하게 걸려 있음)
	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	Optional<User> findByEmail(String email);
}
