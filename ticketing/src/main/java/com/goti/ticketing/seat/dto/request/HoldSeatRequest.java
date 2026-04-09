package com.goti.ticketing.seat.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HoldSeatRequest(
	@Schema(description = "경기 ID", example = "11111111-1111-1111-1111-111111111111")
	@NotNull(message = "경기 ID는 필수입니다.")
	UUID gameId,

	@Schema(description = "대기열 토큰", example = "eyJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0...")
	@NotBlank(message = "대기열 토큰은 필수입니다.")
	@JsonAlias("queueTokenJti")
	String queueToken
) {
}
