package com.goti.ticketing.seat.repository;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;

public interface SeatStatusRepositoryCustom {
	List<SeatStatusEntity> findSeatStatuses(UUID gameId, List<UUID> seatIds);

	List<UUID> findSeatIdsByGameId(UUID gameId);
}
