package com.pbit.api.domain.term.entity;

import java.time.Instant;

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
@Table(name = "terms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TermCode code;

	@Column(nullable = false, length = 10)
	private String version;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false)
	private boolean required;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "effective_at", nullable = false)
	private Instant effectiveAt;

}
