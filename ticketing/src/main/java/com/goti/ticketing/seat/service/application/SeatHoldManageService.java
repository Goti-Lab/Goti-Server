package com.goti.ticketing.seat.service.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.infra.lock.DistributedLockManager;
import com.goti.ticketing.queue.infra.QueueTokenPayload;
import com.goti.ticketing.queue.infra.QueueTokenReader;
import com.goti.global.validation.Preconditions;
import com.goti.ticketing.seat.service.domain.SeatHoldService;
import com.goti.ticketing.session.service.application.ReservationSessionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatHoldManageService {
	private final SeatHoldService seatHoldService;
	private final DistributedLockManager distributedLockManager;
	private final SeatHoldTransactionalService seatHoldTransactionalService;
	private final ReservationSessionService reservationSessionService;
	private final QueueTokenReader queueTokenReader;

	public UUID hold(UUID gameId, UUID seatId, UUID userId, String queueToken) {
		reservationSessionService.validateActiveSession(userId, gameId);
		QueueTokenPayload queueTokenPayload = queueTokenReader.parse(queueToken);
		validateQueueToken(gameId, userId, queueTokenPayload);

		String lockKey = buildLockKey(gameId, seatId);
		return distributedLockManager.withLock(
			lockKey,
			() -> seatHoldTransactionalService.hold(
				gameId,
				seatId,
				userId,
				queueTokenPayload.tokenId().toString()
			)
		);
	}

	public UUID release(UUID holdId, UUID userId) {
		SeatHoldEntity seatHold = seatHoldService.findSeatHold(holdId);

		String lockKey = buildLockKey(seatHold.getGameSchedule().getId(), seatHold.getSeat().getId());
		return distributedLockManager.withLock(lockKey, () -> seatHoldTransactionalService.release(holdId, userId));
	}

	public UUID release(UUID gameId, UUID holdId, UUID userId) {
		SeatHoldEntity seatHold = seatHoldService.findSeatHold(holdId);

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

	private static void validateQueueToken(UUID gameId, UUID userId, QueueTokenPayload queueTokenPayload) {
		if (!queueTokenPayload.gameId().equals(gameId) || !queueTokenPayload.userId().equals(userId)) {
			throw new CustomException(ErrorCode.QUEUE_ENTRY_MISMATCH);
		}
	}
}
