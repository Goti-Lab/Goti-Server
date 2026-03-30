package com.goti.resale.utils;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.goti.infra.lock.DistributedLockManager;
import com.goti.resale.config.properties.ResaleHoldExpiryProperties;
import com.goti.resale.service.application.ResaleHoldExpiryBatchResult;
import com.goti.resale.service.application.ResaleHoldExpiryProcessService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
	prefix = "seat.hold-expiry",
	name = "enabled",
	havingValue = "true",
	matchIfMissing = true
)
public class ResaleHoldScheduler {
	private static final String EXPIRY_JOB_LOCK_KEY = "lock:resale-expiry-job";

	private final ResaleHoldExpiryProcessService resaleHoldExpiryProcessService;
	private final DistributedLockManager distributedLockManager;
	private final ResaleHoldExpiryProperties resaleHoldExpiryProperties;

	@Scheduled(fixedDelayString = "${seat.hold-expiry.fixed-delay-ms}")
	public void expireHolds() {
		boolean acquired = distributedLockManager.withLockIfAvailable(
			EXPIRY_JOB_LOCK_KEY,
			() -> {
				ResaleHoldExpiryBatchResult result = resaleHoldExpiryProcessService.expireHolds(
					resaleHoldExpiryProperties.batchSize()
				);

				if (result.attempted() > 0) {
					log.info(
						"리셀 점유 만료 처리 완료. attempted={}, succeeded={}, failed={}",
						result.attempted(),
						result.succeeded(),
						result.failed()
					);
				}

				if (result.failed() > 0) {
					log.warn(
						"리셀 점유 만료 처리 중 실패 발생. attempted={}, succeeded={}, failed={}",
						result.attempted(),
						result.succeeded(),
						result.failed()
					);
				}
			}
		);

		if (!acquired) {
			log.debug("리셀 만료 스케줄러 락을 획득하지 못해 이번 실행을 건너뜁니다. (다른 서버에서 실행 중)");
		}
	}
}