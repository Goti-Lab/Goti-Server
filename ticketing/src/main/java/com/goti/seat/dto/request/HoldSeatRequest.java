package com.goti.seat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HoldSeatRequest(
	@Schema(description = "경기 ID", example = "11111111-1111-1111-1111-111111111111")
	@NotNull(message = "경기 ID는 필수입니다.")
	UUID gameId,

	@Schema(description = "대기열 토큰 식별자", example = "queue-token-jti-123")
	@NotBlank(message = "큐 토큰 식별자는 필수입니다.")
	String queueTokenJti
) {
}
