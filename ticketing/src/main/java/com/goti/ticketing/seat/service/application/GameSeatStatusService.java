package com.goti.ticketing.seat.service.application;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.constants.SeatStatus;
import com.goti.ticketing.seat.dto.response.GameSeatStatusResponse;

import lombok.extern.slf4j.Slf4j;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class GameSeatStatusService {
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

	@Transactional(readOnly = true)
	public List<GameSeatStatusResponse> get(UUID gameId, UUID sectionId, UUID userId) {
		Preconditions.validate(
			userId != null,
			ErrorCode.AUTH_INVALID
		);

		List<UUID> seatIds = seatService.getSeatIdsBySectionId(sectionId);
		Map<UUID, SeatStatusEntity> seatStatusesBySeatId = seatStatusService.getByGameIdAndSeatIds(gameId, seatIds);

		List<GameSeatStatusResponse> seatStatuses = seatIds.stream()
			.map(seatStatusesBySeatId::get)
			.filter(java.util.Objects::nonNull)
			.map(GameSeatStatusResponse::from)
			.toList();

		Map<SeatStatus, Long> counts = seatStatuses.stream()
			.collect(Collectors.groupingBy(GameSeatStatusResponse::status, Collectors.counting()));

		log.info(
			"action=SEAT_STATUS gameId={} userId={} sectionId={} total={} available={} held={} sold={} blocked={}",
			gameId,
			userId,
			sectionId,
			seatStatuses.size(),
			counts.getOrDefault(SeatStatus.AVAILABLE, 0L),
			counts.getOrDefault(SeatStatus.HELD, 0L),
			counts.getOrDefault(SeatStatus.SOLD, 0L),
			counts.getOrDefault(SeatStatus.BLOCKED, 0L)
		);

		return seatStatuses;
	}
}
