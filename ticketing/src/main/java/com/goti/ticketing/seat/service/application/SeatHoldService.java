package com.goti.ticketing.seat.service.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.exception.CustomException;
import com.goti.infra.lock.DistributedLockManager;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.seat.repository.SeatHoldRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatHoldService {
	private final SeatHoldRepository seatHoldRepository;
	private final DistributedLockManager distributedLockManager;
	private final SeatHoldTransactionalService seatHoldTransactionalService;

	public UUID hold(UUID gameId, UUID seatId, UUID userId, String queueTokenJti) {
		String lockKey = buildLockKey(gameId, seatId);
		return distributedLockManager.withLock(
			lockKey,
			() -> seatHoldTransactionalService.hold(gameId, seatId, userId, queueTokenJti)
		);
	}

	public UUID release(UUID holdId, UUID userId) {
		SeatHoldEntity seatHold = seatHoldRepository.findById(holdId)
			.orElseThrow(() -> new CustomException(ErrorCode.SEAT_HOLD_NOT_FOUND));

		String lockKey = buildLockKey(seatHold.getGameSchedule().getId(), seatHold.getSeat().getId());
		return distributedLockManager.withLock(lockKey, () -> seatHoldTransactionalService.release(holdId, userId));
	}

	public UUID release(UUID gameId, UUID holdId, UUID userId) {
		SeatHoldEntity seatHold = seatHoldRepository.findById(holdId)
			.orElseThrow(() -> new CustomException(ErrorCode.SEAT_HOLD_NOT_FOUND));

		Preconditions.validate(
			seatHold.getGameSchedule().getId().equals(gameId),
			ErrorCode.SEAT_HOLD_GAME_MISMATCH
		);

		String lockKey = buildLockKey(gameId, seatHold.getSeat().getId());
		return distributedLockManager.withLock(lockKey, () -> seatHoldTransactionalService.release(holdId, userId));
	}

	private static String buildLockKey(UUID gameId, UUID seatId) {
		return "lock:seat:" + gameId + ":" + seatId;
	}
}
