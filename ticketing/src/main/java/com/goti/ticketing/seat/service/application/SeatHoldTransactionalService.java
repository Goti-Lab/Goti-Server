package com.goti.ticketing.seat.service.application;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.goti.exception.CustomException;
import com.goti.ticketing.game.repository.gameschedule.GameScheduleRepository;
import com.goti.ticketing.constants.SeatStatus;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.seat.config.properties.SeatHoldProperties;
import com.goti.ticketing.seat.repository.SeatHoldRepository;
import com.goti.ticketing.seat.repository.SeatRepository;
import com.goti.ticketing.seat.repository.SeatStatusRepository;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatHoldTransactionalService {
	private final GameScheduleRepository gameScheduleRepository;
	private final SeatRepository seatRepository;
	private final SeatStatusRepository seatStatusRepository;
	private final SeatHoldRepository seatHoldRepository;
	private final SeatHoldProperties seatHoldProperties;

	@Transactional
	public UUID hold(UUID gameId, UUID seatId, UUID userId, String queueTokenJti) {
		SeatStatusEntity seatStatus = seatStatusRepository.findByGameAndSeat(
				gameScheduleRepository.getReferenceById(gameId),
				seatRepository.getReferenceById(seatId)
			)
			.orElseThrow(() -> blocked(gameId, userId, seatId, ErrorCode.SEAT_STATUS_NOT_FOUND));

		if (seatStatus.getStatus() != SeatStatus.AVAILABLE) {
			throw blocked(gameId, userId, seatId, ErrorCode.SEAT_ALREADY_SELECTED);
		}

		seatStatus.hold();
		seatStatusRepository.save(seatStatus);

		// TODO: 좌석 점유 시 game_seat_inventories 카운트 반영
		SeatHoldEntity seatHold = SeatHoldEntity.create(
			seatStatus.getSeat(),
			seatStatus.getGame(),
			userId,
			queueTokenJti,
			LocalDateTime.now().plus(seatHoldProperties.ttl())
		);

		UUID holdId = seatHoldRepository.save(seatHold).getId();
		log.info("action=SEAT_HOLD gameId={} userId={} seatId={} holdId={}", gameId, userId, seatId, holdId);
		return holdId;
	}

	private CustomException blocked(UUID gameId, UUID userId, UUID seatId, ErrorCode errorCode) {
		return new CustomException(errorCode)
			.withContext("action", "SEAT_HOLD_BLOCKED")
			.withContext("gameId", gameId)
			.withContext("userId", userId)
			.withContext("seatId", seatId);
	}

	@Transactional
	public UUID release(UUID holdId, UUID userId) {
		SeatHoldEntity seatHold = seatHoldRepository.findHoldWithSeatAndGame(holdId)
			.orElseThrow(() -> new CustomException(ErrorCode.SEAT_HOLD_NOT_FOUND));

		Preconditions.validate(
			seatHold.getUserId().equals(userId),
			ErrorCode.AUTH_PERMISSION_DENIED
		);

		SeatStatusEntity seatStatus = seatStatusRepository.findByGameAndSeat(
			seatHold.getGameSchedule(),
			seatHold.getSeat()
		).orElseThrow(() -> new CustomException(ErrorCode.SEAT_STATUS_NOT_FOUND));

		seatStatus.release();
		seatHold.release();

		// TODO: 좌석 해제 시 game_seat_inventories 카운트 반영
		seatStatusRepository.save(seatStatus);
		seatHoldRepository.save(seatHold);

		return seatHold.getId();
	}
}
