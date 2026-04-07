package com.goti.ticketing.seat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.goti.ticketing.constants.SeatHoldStatus;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;

public interface SeatHoldRepositoryCustom {
	List<SeatHoldEntity> findAllWithDetailsByIdIn(List<UUID> holdIds);

	List<SeatHoldEntity> findHoldsWithSeatAndGame(SeatHoldStatus status, LocalDateTime now, int limit);

	Optional<SeatHoldEntity> findHoldWithSeatAndGame(UUID holdId);

	List<SeatHoldEntity> findAllHoldingSeats(UUID gameId, UUID userId);
}
