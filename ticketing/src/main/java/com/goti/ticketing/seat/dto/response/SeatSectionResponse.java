package com.goti.ticketing.seat.dto.response;

import java.util.UUID;

import com.goti.ticketing.domain.entity.seat.SeatSectionEntity;

import io.swagger.v3.oas.annotations.media.Schema;

public record SeatSectionResponse(
	@Schema(description = "좌석 구역 ID", example = "33333333-3333-3333-3333-333333333333")
	UUID sectionId,

	@Schema(description = "좌석 등급 ID", example = "11111111-1111-1111-1111-111111111111")
	UUID gradeId,

	@Schema(description = "구장 ID", example = "22222222-2222-2222-2222-222222222222")
	UUID stadiumId,

	@Schema(description = "구역 코드", example = "T3-1")
	String sectionCode,

	@Schema(description = "수용 인원", example = "120")
	Integer capacity
) {
	public static SeatSectionResponse from(SeatSectionEntity seatSection) {
		return new SeatSectionResponse(
			seatSection.getId(),
			seatSection.getSeatGrade().getId(),
			seatSection.getStadiumId(),
			seatSection.getSectionCode(),
			seatSection.getCapacity()
		);
	}
}
