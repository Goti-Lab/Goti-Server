package com.goti.seat.dto.response;

import java.util.UUID;

import com.goti.constants.SeatStatus;
import com.goti.domain.entity.seat.SeatStatusEntity;

import io.swagger.v3.oas.annotations.media.Schema;

public record GameSeatStatusResponse(
	@Schema(description = "좌석 ID", example = "44444444-4444-4444-4444-444444444444")
	UUID seatId,

	@Schema(description = "좌석 상태", example = "AVAILABLE")
	SeatStatus status
) {
	public static GameSeatStatusResponse from(SeatStatusEntity seatStatus) {
		return new GameSeatStatusResponse(
			seatStatus.getSeat().getId(),
			seatStatus.getStatus()
		);
	}
}
