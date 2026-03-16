package com.goti.ticketing.seat.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.seat.dto.response.BulkCreateSeatsResponse;
import com.goti.ticketing.seat.dto.response.SeatResponse;

public interface SeatService {
	BulkCreateSeatsResponse create(UUID sectionId, String rowName, Integer startSeatNumber, Integer endSeatNumber);

	List<SeatResponse> get(UUID sectionId, UUID userId);
}
