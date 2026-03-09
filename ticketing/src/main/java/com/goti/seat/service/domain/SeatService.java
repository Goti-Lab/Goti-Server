package com.goti.seat.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.seat.dto.response.BulkCreateSeatsResponse;

public interface SeatService {
	BulkCreateSeatsResponse create(UUID sectionId, String rowName, Integer startSeatNumber, Integer endSeatNumber);
}
