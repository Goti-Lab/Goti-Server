package com.goti.ticketing.seat.service.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.seat.SeatEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;

public interface SeatStatusService {
	Map<UUID, SeatStatusEntity> getByGameIdAndSeatIds(UUID gameId, List<UUID> seatIds);

	Set<UUID> getSeatIdsByGameId(UUID gameId);

	List<SeatStatusEntity> createMissingStatuses(GameScheduleEntity game, List<SeatEntity> seats, Set<UUID> existingSeatIds);

	SeatStatusEntity get(GameScheduleEntity game, SeatEntity seat);

	void expire(SeatStatusEntity seatStatus);

	long countAvailableSeats(GameScheduleEntity gameSchedule);

	void cancelSale(SeatStatusEntity seatStatus);
}
