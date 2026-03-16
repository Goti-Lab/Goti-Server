package com.goti.ticketing.seat.service.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.goti.exception.CustomException;
import com.goti.ticketing.seat.repository.SeatHoldRepository;
import com.goti.ticketing.seat.repository.SeatStatusRepository;
import com.goti.ticketing.seat.service.domain.SeatHoldExpiryService;

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

		SeatStatusEntity seatStatus = seatStatusRepository.findByGameAndSeat(
			seatHold.getGameSchedule(),
			seatHold.getSeat()
		)
			.orElseThrow(() -> new CustomException(ErrorCode.SEAT_STATUS_NOT_FOUND));

		seatHoldExpiryService.expire(seatStatus, seatHold, now);
		seatStatusRepository.save(seatStatus);
		seatHoldRepository.save(seatHold);
	}
}
