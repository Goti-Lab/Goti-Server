package com.goti.seat.service.domain;

import java.util.List;
import java.util.UUID;

import com.goti.seat.dto.response.GameSeatStatusResponse;

public interface SeatStatusService {
	List<GameSeatStatusResponse> get(UUID gameId, UUID sectionId, UUID userId);
}
