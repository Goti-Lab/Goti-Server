package com.goti.ticketing.seat.service.application;

import com.goti.ticketing.constants.SeatHoldStatus;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.goti.infra.lock.DistributedLockManager;
import com.goti.ticketing.seat.repository.SeatHoldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeatHoldExpiryService {
	private final SeatHoldRepository seatHoldRepository;
	private final DistributedLockManager distributedLockManager;
	private final SeatHoldExpiryTransactionalService seatHoldExpiryTransactionalService;

	// TODO: 동시성 및 hold/release/expire 경합 상황 테스트 추가
	public SeatHoldExpiryBatchResult expireHolds(int batchSize) {
		LocalDateTime now = LocalDateTime.now();
		List<SeatHoldEntity> expiredHolds = seatHoldRepository.findHoldsWithSeatAndGame(
			SeatHoldStatus.HOLDING,
			now,
			batchSize
		);

		int succeeded = 0;
		int failed = 0;
		for (SeatHoldEntity seatHold : expiredHolds) {
			try {
				boolean acquired = expireOne(seatHold, now);
				if (acquired) {
					succeeded++;
					continue;
				}

				failed++;
				log.debug(
					"action=LOCK_TIMEOUT operation=SEAT_HOLD_EXPIRY gameId={} seatId={} holdId={}",
					seatHold.getGameSchedule().getId(),
					seatHold.getSeat().getId(),
					seatHold.getId()
				);
			} catch (Exception e) {
				failed++;
				log.warn(
					"action=HOLD_RELEASE_FAILED gameId={} seatId={} holdId={} reason={}",
					seatHold.getGameSchedule().getId(),
					seatHold.getSeat().getId(),
					seatHold.getId(),
					e.getMessage()
				);
			}
		}

		return new SeatHoldExpiryBatchResult(expiredHolds.size(), succeeded, failed);
	}

	private boolean expireOne(
		SeatHoldEntity seatHold,
		LocalDateTime now
	) {
		UUID gameId = seatHold.getGameSchedule().getId();
		UUID seatId = seatHold.getSeat().getId();
		String lockKey = buildLockKey(gameId, seatId);

		return distributedLockManager.withLockIfAvailable(
			lockKey,
			() -> seatHoldExpiryTransactionalService.expire(seatHold.getId(), now)
		);
	}

	private static String buildLockKey(UUID gameId, UUID seatId) {
		return "lock:seat:" + gameId + ":" + seatId;
	}
}
