package com.goti.seat.service.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.seat.SeatHoldEntity;
import com.goti.domain.entity.seat.SeatStatusEntity;
import com.goti.exception.CustomException;
import com.goti.seat.repository.SeatHoldRepository;
import com.goti.seat.repository.SeatStatusRepository;
import com.goti.seat.service.domain.SeatHoldExpiryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatHoldExpiryTransactionalService {
	private final SeatHoldRepository seatHoldRepository;
	private final SeatStatusRepository seatStatusRepository;
	private final SeatHoldExpiryService seatHoldExpiryService;

	@Transactional
	public void expire(UUID holdId, LocalDateTime now) {
		SeatHoldEntity seatHold = seatHoldRepository.findById(holdId)
			.orElseThrow(() -> new CustomException(ErrorCode.SEAT_HOLD_NOT_FOUND));

		UUID gameId = seatHold.getGameSchedule().getId();
		UUID seatId = seatHold.getSeat().getId();

		SeatStatusEntity seatStatus = seatStatusRepository.findByGame_IdAndSeat_Id(gameId, seatId)
			.orElseThrow(() -> new CustomException(ErrorCode.SEAT_STATUS_NOT_FOUND));

		seatHoldExpiryService.expire(seatStatus, seatHold, now);
		seatStatusRepository.save(seatStatus);
		seatHoldRepository.save(seatHold);
	}
}
