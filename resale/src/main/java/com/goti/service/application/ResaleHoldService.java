package com.goti.service.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.resale.ResaleHoldEntity;
import com.goti.dto.request.ResaleHoldRequest;
import com.goti.dto.response.ResaleHoldResponse;
import com.goti.dto.response.ResaleReleaseResponse;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.infra.lock.DistributedLockManager;
import com.goti.repository.hold.ResaleHoldRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleHoldService {
	private final ResaleHoldRepository resaleHoldRepository;
	private final DistributedLockManager distributedLockManager;
	private final ResaleHoldTransactionalService resaleHoldTransactionalService;

	public ResaleHoldResponse holdResale(UUID buyerId, ResaleHoldRequest request) {
		String lockKey = buildLockKey(request.listingId());
		return distributedLockManager.withLock(
			lockKey, () -> resaleHoldTransactionalService.hold(buyerId, request));
	}

	public ResaleReleaseResponse releaseResaleHold(UUID buyerId, UUID holdId) {
		ResaleHoldEntity resaleHold = resaleHoldRepository.findById(holdId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.RESALE_HOLD_NOT_FOUND)
			);
		Preconditions.validate(resaleHold.getUserId().equals(buyerId), ErrorCode.AUTH_PERMISSION_DENIED);

		String lockKey = buildLockKey(resaleHold.getResaleListing().getId());

		return distributedLockManager.withLock(
			lockKey,
			() -> resaleHoldTransactionalService.release(holdId)
		);
	}

	private static String buildLockKey(UUID listingId) {
		return "lock:resale:" + listingId;
	}
}
