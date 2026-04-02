package com.goti.ticketing.seat.service.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.seat.SeatEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.goti.ticketing.seat.dto.response.GameSeatStatusResponse;

public interface SeatStatusService {
	List<GameSeatStatusResponse> get(UUID gameId, UUID sectionId, UUID userId);

	Map<UUID, SeatStatusEntity> getByGameIdAndSeatIds(UUID gameId, List<UUID> seatIds);

	Set<UUID> getSeatIdsByGameId(UUID gameId);

	List<SeatStatusEntity> createMissingStatuses(GameScheduleEntity game, List<SeatEntity> seats, Set<UUID> existingSeatIds);

	SeatStatusEntity get(GameScheduleEntity game, SeatEntity seat);

	long countAvailableSeats(GameScheduleEntity gameSchedule);

	void cancelSale(SeatStatusEntity seatStatus);

	void release(SeatStatusEntity seatStatus);
}
