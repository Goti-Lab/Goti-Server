package com.goti.ticketing.seat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record ReleaseSeatResponse(
	@Schema(description = "해제된 좌석 점유 ID", example = "05139a4c-93b3-44a2-95b9-84d1ffd50145")
	UUID holdId
) {
	public static ReleaseSeatResponse from(UUID holdId) {
		return new ReleaseSeatResponse(holdId);
	}
}