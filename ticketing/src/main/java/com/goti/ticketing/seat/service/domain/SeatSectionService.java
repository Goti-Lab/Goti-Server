package com.goti.ticketing.seat.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.seat.dto.response.SeatSectionRegisterResponse;
import com.goti.ticketing.seat.dto.response.SeatSectionSearchResponse;

public interface SeatSectionService {
	SeatSectionRegisterResponse create(UUID gradeId, UUID stadiumId, String sectionCode, Integer capacity);

	List<SeatSectionSearchResponse> get(UUID stadiumId, UUID userId, UUID gameId);
}
