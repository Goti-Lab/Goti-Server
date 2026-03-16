package com.goti.ticketing.seat.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.seat.dto.response.SeatGradeResponse;

public interface SeatGradeService {
	SeatGradeResponse create(UUID stadiumId, String name, String displayColorHex);

	List<SeatGradeResponse> get(UUID stadiumId, UUID userId);
}
