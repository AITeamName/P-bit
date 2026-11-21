package com.pbit.api.domain.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pbit.api.domain.user.entity.UserTermAgreement;

public interface UserTermAgreementRepository extends JpaRepository<UserTermAgreement, Long> {

	List<UserTermAgreement> findAllByUserId(UUID userId);

}
