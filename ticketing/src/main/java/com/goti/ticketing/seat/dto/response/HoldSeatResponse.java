package com.goti.ticketing.seat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record HoldSeatResponse(
	@Schema(description = "생성된 좌석 점유 ID", example = "05139a4c-93b3-44a2-95b9-84d1ffd50145")
	UUID holdId
) {
	public static HoldSeatResponse from(UUID holdId) {
		return new HoldSeatResponse(holdId);
	}
}
