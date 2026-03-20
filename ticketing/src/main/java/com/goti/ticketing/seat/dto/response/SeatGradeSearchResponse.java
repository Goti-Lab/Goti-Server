package com.goti.ticketing.seat.dto.response;

import java.util.UUID;

import com.goti.ticketing.domain.entity.seat.SeatGradeEntity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 등급 조회 응답")
public record SeatGradeSearchResponse(
	@Schema(description = "좌석 등급 ID", example = "22222222-2222-2222-2222-222222222222")
	UUID seatGradeId,

	@Schema(description = "구장 ID", example = "11111111-1111-1111-1111-111111111111")
	UUID stadiumId,

	@Schema(description = "좌석 등급명", example = "VIP")
	String name,

	@Schema(description = "좌석 등급 표시 색상 HEX", example = "#FFAA00")
	String displayColorHex,

	@Schema(description = "잔여석 수", example = "87")
	Integer availableSeatCount
) {
	public static SeatGradeSearchResponse from(
		SeatGradeEntity seatGrade,
		Integer availableSeatCount
	) {
		return new SeatGradeSearchResponse(
			seatGrade.getId(),
			seatGrade.getStadiumId(),
			seatGrade.getName(),
			seatGrade.getDisplayColorHex(),
			availableSeatCount
		);
	}
}
