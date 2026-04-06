package com.goti.ticketing.seat.dto.response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 일괄 생성 응답")
public record BulkCreateSeatsResponse(
	@Schema(description = "좌석 구역 ID", example = "7b1e6d4a-8a0d-4d6b-8ec1-1f4ef6a21001")
	UUID sectionId,
	@Schema(description = "행 이름", example = "A")
	String rowName,
	@Schema(description = "시작 좌석 번호", example = "1")
	Integer startSeatNumber,
	@Schema(description = "종료 좌석 번호", example = "10")
	Integer endSeatNumber
) {
	public static BulkCreateSeatsResponse from(
		UUID sectionId,
		String rowName,
		Integer startSeatNumber,
		Integer endSeatNumber
	) {
		return new BulkCreateSeatsResponse(
			sectionId,
			rowName,
			startSeatNumber,
			endSeatNumber
		);
	}
}
