package com.goti.seat.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.seat.dto.response.SeatSectionResponse;

public interface SeatSectionService {
	SeatSectionResponse create(UUID gradeId, UUID stadiumId, String sectionCode, Integer capacity);

	List<SeatSectionResponse> get(UUID stadiumId, UUID userId);
}
