package com.goti.ticketing.seat.service.application;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.ticketing.seat.handler.GameSeatUpdateHandler;

import com.goti.ticketing.seat.service.domain.SeatStatusService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.goti.exception.CustomException;
import com.goti.ticketing.seat.repository.SeatHoldRepository;
import com.goti.ticketing.seat.repository.SeatStatusRepository;
import com.goti.ticketing.seat.service.domain.SeatHoldService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatHoldExpiryTransactionalService {
	private final SeatHoldRepository seatHoldRepository;
	private final SeatStatusRepository seatStatusRepository;

	private final SeatHoldService seatHoldService;
	private final SeatStatusService seatStatusService;

	private final GameSeatUpdateHandler gameSeatUpdateHandler;

	@Transactional
	public void expire(UUID holdId, LocalDateTime now) {
		SeatHoldEntity seatHold = seatHoldRepository.findHoldWithSeatAndGame(holdId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.SEAT_HOLD_NOT_FOUND)
			);

		SeatStatusEntity seatStatus = seatStatusRepository.findByGameAndSeat(
			seatHold.getGameSchedule(),
			seatHold.getSeat()
		).orElseThrow(
			() -> new CustomException(ErrorCode.SEAT_STATUS_NOT_FOUND)
		);
		UUID gameId = seatStatus.getGame().getId();
		seatHoldService.expire(seatHold, now);
		seatStatusService.expire(seatStatus);
		gameSeatUpdateHandler.onSeatIncrease(gameId, 1);
	}
}
