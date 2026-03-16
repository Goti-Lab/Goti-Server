package com.goti.ticketing.seat.dto.response;

import java.util.UUID;

import com.goti.ticketing.domain.entity.seat.SeatEntity;

import io.swagger.v3.oas.annotations.media.Schema;

public record SeatResponse(
	@Schema(description = "좌석 ID", example = "44444444-4444-4444-4444-444444444444")
	UUID seatId,

	@Schema(description = "좌석 구역 ID", example = "33333333-3333-3333-3333-333333333333")
	UUID sectionId,

	@Schema(description = "행 이름", example = "A")
	String rowName,

	@Schema(description = "좌석 번호", example = "12")
	Integer seatNum,

	@Schema(description = "좌석 사용 가능 여부", example = "true")
	boolean available
) {
	public static SeatResponse from(SeatEntity seat) {
		return new SeatResponse(
			seat.getId(),
			seat.getSeatSection().getId(),
			seat.getRowName(),
			seat.getSeatNum(),
			seat.isAvailable()
		);
	}
}
