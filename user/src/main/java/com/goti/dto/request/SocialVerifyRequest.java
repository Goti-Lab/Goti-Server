package com.goti.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SocialVerifyRequest(
	@NotBlank(message = "인증 코드는 필수 항목입니다.")
	String authCode,

	String state
) {
}
