package com.goti.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResaleHoldRequest(
	@Schema(description = "리셀 ID", example = "8df84c70-833e-4374-85ad-fa52f92f939e")
	@NotNull(message = "리셀 ID는 필수입니다.")
	UUID listingId,

	@Schema(description = "대기열 토큰 식별자", example = "queue-token-jti-111")
	@NotBlank(message = "대기열 토큰 식별자는 필수입니다.")
	String queueTokenJti
) {
}
