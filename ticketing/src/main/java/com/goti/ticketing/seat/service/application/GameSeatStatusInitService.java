package com.goti.ticketing.seat.service.application;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.seat.SeatEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.goti.ticketing.game.service.domain.GameScheduleService;
import com.goti.ticketing.seat.dto.response.GameSeatStatusInitResponse;
import com.goti.ticketing.seat.service.domain.SeatService;
import com.goti.ticketing.seat.service.domain.SeatStatusService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameSeatStatusInitService {
	private final GameScheduleService gameScheduleService;
	private final SeatService seatService;
	private final SeatStatusService seatStatusService;

	@Transactional
	public GameSeatStatusInitResponse init(UUID gameId) {
		GameScheduleEntity game = gameScheduleService.get(gameId);
		List<SeatEntity> seats = seatService.getByStadiumId(game.getStadiumId());
		Set<UUID> existingSeatIds = seatStatusService.getSeatIdsByGameId(gameId);
		List<SeatStatusEntity> newSeatStatuses = seatStatusService.createMissingStatuses(game, seats, existingSeatIds);

		return new GameSeatStatusInitResponse(
			gameId,
			newSeatStatuses.size(),
			seats.size() - newSeatStatuses.size()
		);
	}
}
