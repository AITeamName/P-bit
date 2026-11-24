package com.pbit.api.domain.user.dto.response;

import com.pbit.api.domain.user.entity.AuthProvider;
import com.pbit.api.domain.user.entity.Domain;
import com.pbit.api.domain.user.entity.Level;
import com.pbit.api.domain.user.entity.SelfReportedLevel;
import com.pbit.api.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record UserMeResponse(
		@Schema(description = "회원 ID")
		UUID userId,
		@Schema(description = "회원 email")
		String email,
		@Schema(description = "닉네임")
		String nickname,
		@Schema(description = "가입 방식")
		AuthProvider provider,
		@Schema(description = "AI 사용 방식")
		Domain domain,
		@Schema(description = "자가 신고 레벨")
		SelfReportedLevel selfReportedLevel,
		@Schema(description = "현재 레벨")
		Level currentLevel,
		@Schema(description = "레벨 측정 시각")
		Instant levelMeasuredAt,
		@Schema(description = "온보딩 완료 여부")
		boolean onboardingCompleted
) {
	public static UserMeResponse from(User user) {
		return new UserMeResponse(
				user.getId(),
				user.getEmail(),
				user.getNickname(),
				user.getProvider(),
				user.getDomain(),
				user.getSelfReportedLevel(),
				user.getCurrentLevel(),
				user.getLevelMeasuredAt(),
				user.isOnboardingCompleted()
				);
	}
}
