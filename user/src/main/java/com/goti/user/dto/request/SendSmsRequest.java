package com.goti.user.dto.request;

import com.goti.config.validator.ValidMobile;

import jakarta.validation.constraints.NotBlank;

public record SendSmsRequest(
	@NotBlank(message = "소셜 검증 토큰은 필수입니다.")
	String socialVerifyToken,

	@ValidMobile
	@NotBlank(message = "휴대전화번호는 필수 항목입니다.")
	String mobile
) {}
