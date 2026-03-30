package com.goti.resale.service.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.goti.infra.lock.DistributedLockManager;
import com.goti.resale.constants.ResaleHoldStatus;
import com.goti.resale.domain.entity.resale.ResaleHoldEntity;
import com.goti.resale.repository.hold.ResaleHoldRepository;
import com.goti.resale.service.domain.ResaleHoldExpiryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleHoldExpiryProcessService {
	private final ResaleHoldRepository resaleHoldRepository;
	private final DistributedLockManager distributedLockManager;
	private final ResaleHoldExpiryService resaleHoldExpiryService;

	public ResaleHoldExpiryBatchResult expireHolds(int batchSize) {
		LocalDateTime now = LocalDateTime.now();

		List<ResaleHoldEntity> expiredHolds = resaleHoldRepository.findExpiredResaleHolds(
			ResaleHoldStatus.HOLDING,
			now,
			PageRequest.of(0, batchSize)
		);

		int succeeded = 0;
		int failed = 0;

		for (ResaleHoldEntity resaleHold : expiredHolds) {
			try {
				boolean acquired = expireOne(resaleHold, now);
				if (acquired) {
					succeeded++;
					continue;
				}

				failed++;
				log.debug(
					"리셀 점유 만료 처리 락 획득 실패로 건너뜀. holdId={}, listingId={}",
					resaleHold.getId(),
					resaleHold.getResaleListing().getId()
				);
			} catch (Exception e) {
				failed++;
				log.warn(
					"리셀 점유 만료 처리 실패. holdId={}, listingId={}, reason={}",
					resaleHold.getId(),
					resaleHold.getResaleListing().getId(),
					e.getMessage()
				);
			}
		}

		return new ResaleHoldExpiryBatchResult(expiredHolds.size(), succeeded, failed);
	}

	private boolean expireOne(ResaleHoldEntity resaleHold, LocalDateTime now) {
		UUID listingId = resaleHold.getResaleListing().getId();
		String lockKey = buildLockKey(listingId);

		return distributedLockManager.withLockIfAvailable(
			lockKey,
			() -> resaleHoldExpiryService.expire(resaleHold.getId(), now)
		);
	}

	private static String buildLockKey(UUID listingId) {
		return "lock:resale:" + listingId;
	}
}