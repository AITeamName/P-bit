package com.pbit.api.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbit.api.domain.user.dto.response.UserMeResponse;
import com.pbit.api.domain.user.service.UserService;
import com.pbit.api.global.auth.AuthUser;
import com.pbit.api.global.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "회원", description = "내 정보")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(summary = "내 정보 조회", description = "로그인 사용자 정보 + 현재 레벨 (사이드바 헤더, MY). "
		+ "온보딩 전 회원은 domain·selfReportedLevel·currentLevel·levelMeasuredAt이 null")
	@ApiResponse(responseCode = "200", description = "내 정보")
	@ApiResponse(responseCode = "401", description = "TOKEN_EXPIRED 토큰 만료 / TOKEN_INVALID 토큰 없음·위조·탈퇴 회원",
		content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@GetMapping("/me")
	public ResponseEntity<UserMeResponse> getUserMe(@AuthenticationPrincipal AuthUser authUser) {
		// 사용자 ID는 요청 값이 아니라 토큰에서만 꺼낸다
		return ResponseEntity.ok(userService.getUserMe(authUser.userId()));
	}

}
