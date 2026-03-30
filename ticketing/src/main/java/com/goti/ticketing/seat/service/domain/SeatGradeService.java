package com.goti.ticketing.seat.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.seat.dto.response.SeatGradeRegisterResponse;
import com.goti.ticketing.seat.dto.response.SeatGradeSearchResponse;
import com.goti.ticketing.seat.dto.response.SeatGradeSearchResultResponse;

public interface SeatGradeService {
	SeatGradeRegisterResponse create(UUID stadiumId, String name, String displayColorHex);

	SeatGradeSearchResultResponse get(UUID stadiumId, UUID gameId, UUID userId);
}
