package com.goti.ticketing.seat.utils;

import com.goti.infra.lock.DistributedLockManager;
import com.goti.ticketing.seat.config.properties.SeatHoldExpiryProperties;
import com.goti.ticketing.seat.service.application.SeatHoldExpiryService;
import com.goti.ticketing.seat.service.application.SeatHoldExpiryBatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
	prefix = "seat.hold-expiry",
	name = "enabled",
	havingValue = "true",
	matchIfMissing = true
)
public class SeatHoldExpiryScheduler {
	private static final String EXPIRY_JOB_LOCK_KEY = "lock:seat-expiry-job";

	private final SeatHoldExpiryService seatHoldExpiryService;
	private final DistributedLockManager distributedLockManager;
	private final SeatHoldExpiryProperties seatHoldExpiryProperties;

	// @Scheduled(fixedDelayString = "${seat.hold-expiry.fixed-delay-ms}")
	public void expireHolds() {
		boolean acquired = distributedLockManager.withLockIfAvailable(
			EXPIRY_JOB_LOCK_KEY,
			() -> {
				SeatHoldExpiryBatchResult result = seatHoldExpiryService.expireHolds(
					seatHoldExpiryProperties.batchSize()
				);

				if (result.attempted() > 0) {
					log.info(
						"좌석 점유 만료 처리 완료. attempted={}, succeeded={}, failed={}",
						result.attempted(),
						result.succeeded(),
						result.failed()
					);
				}

				if (result.failed() > 0) {
					log.warn(
						"좌석 점유 만료 처리 중 실패 발생. attempted={}, succeeded={}, failed={}",
						result.attempted(),
						result.succeeded(),
						result.failed()
					);
				}
			}
		);

		if (!acquired) {
			log.debug("좌석 만료 스케줄러 락을 획득하지 못해 이번 실행을 건너뜁니다.");
		}
	}
}
