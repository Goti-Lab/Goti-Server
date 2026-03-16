package com.goti.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
	@NotBlank(message = "소셜 검증 토큰은 필수 항목입니다.")
	String socialVerifyToken
) {
}
