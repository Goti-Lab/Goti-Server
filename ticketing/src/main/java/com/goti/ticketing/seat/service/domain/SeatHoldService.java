package com.goti.ticketing.seat.service.domain;

import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SeatHoldService {
	SeatHoldEntity findSeatHold(UUID holdId);

	List<SeatHoldEntity> getHoldingSeats(UUID gameId, UUID userId);

	Map<UUID, SeatHoldEntity> getByIds(List<UUID> holdIds);

	void expire(
		SeatStatusEntity seatStatus,
		SeatHoldEntity seatHold,
		LocalDateTime now
	);
}
