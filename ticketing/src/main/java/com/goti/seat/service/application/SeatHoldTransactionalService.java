package com.goti.seat.service.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.seat.SeatHoldEntity;
import com.goti.domain.entity.seat.SeatStatusEntity;
import com.goti.exception.CustomException;
import com.goti.game.repository.GameScheduleRepository;
import com.goti.global.validation.Preconditions;
import com.goti.seat.config.properties.SeatHoldProperties;
import com.goti.seat.repository.SeatHoldRepository;
import com.goti.seat.repository.SeatRepository;
import com.goti.seat.repository.SeatStatusRepository;

import lombok.RequiredArgsConstructor;

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
			.orElseThrow(() -> new CustomException(ErrorCode.SEAT_STATUS_NOT_FOUND));

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

		return seatHoldRepository.save(seatHold).getId();
	}

	@Transactional
	public UUID release(UUID holdId, UUID userId) {
		SeatHoldEntity seatHold = seatHoldRepository.findById(holdId)
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
