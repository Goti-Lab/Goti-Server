package com.goti.ticketing.seat.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.goti.ticketing.session.model.ReservationSessionCache;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌석 등급 및 세션 만료 조회 응답")
public record SeatGradeSearchResultResponse(
	@Schema(description = "예매 세션 ID")
	UUID sessionId,

	@Schema(description = "예매 세션 만료 시각")
	LocalDateTime sessionExpiresAt,

	@Schema(description = "좌석 등급 목록")
	List<SeatGradeSearchResponse> seatGrades
) {
	public static SeatGradeSearchResultResponse from(
		ReservationSessionCache reservationSession,
		List<SeatGradeSearchResponse> seatGrades
	) {
		return new SeatGradeSearchResultResponse(
			reservationSession.sessionId(),
			reservationSession.expiresAt(),
			seatGrades
		);
	}
}
