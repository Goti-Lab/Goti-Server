package com.goti.ticketing.seat.service.domain;

import java.util.UUID;

import com.goti.ticketing.seat.dto.response.SeatGradeRegisterResponse;
import com.goti.ticketing.seat.dto.response.SeatGradeSearchResultResponse;

public interface SeatGradeService {
	SeatGradeRegisterResponse create(UUID stadiumId, String name, String displayColorHex);

	SeatGradeSearchResultResponse findSeatGrades(UUID gameId, UUID userId, boolean forceNewSession);
}
