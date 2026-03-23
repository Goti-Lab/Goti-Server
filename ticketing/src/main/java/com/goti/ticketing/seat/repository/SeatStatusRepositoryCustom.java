package com.goti.ticketing.seat.repository;

import java.util.List;
import java.util.UUID;

import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;

public interface SeatStatusRepositoryCustom {
	List<SeatStatusEntity> findAllByGameAndSeatIds(UUID gameId, List<UUID> seatIds);
}
