package com.goti.service.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.config.properties.ResaleHoldProperties;
import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.resale.ResaleHoldEntity;
import com.goti.domain.entity.resale.ResaleListingEntity;
import com.goti.dto.request.ResaleHoldRequest;
import com.goti.dto.response.ResaleHoldResponse;
import com.goti.dto.response.ResaleReleaseResponse;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.infra.lock.DistributedLockManager;
import com.goti.repository.ResaleHoldRepository;
import com.goti.repository.listing.ResaleListingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleHoldService {
	private final ResaleListingRepository resaleListingRepository;
	private final ResaleHoldRepository resaleHoldRepository;
	private final DistributedLockManager distributedLockManager;
	private final ResaleHoldProperties resaleHoldProperties;

	@Transactional
	public ResaleHoldResponse holdResale(UUID buyerId, ResaleHoldRequest request) {
		String lockKey = buildLockKey(request.listingId());
		return distributedLockManager.withLock(
			lockKey, () -> {
				ResaleListingEntity resaleListing = resaleListingRepository.findById(request.listingId())
					.orElseThrow(
						() -> new CustomException(ErrorCode.LISTING_NOT_FOUND)
					);

				Preconditions.validate(resaleListing.isPurchasable(), ErrorCode.NOT_PURCHASABLE);

				Preconditions.validate(
					!resaleListing.getSellerId().equals(buyerId),
					ErrorCode.NOT_PURCHASABLE,
					"본인의 티켓은 구매할 수 없습니다."
				);

				resaleListing.hold();

				resaleListingRepository.save(resaleListing);

				ResaleHoldEntity resaleHold = ResaleHoldEntity.create(
					resaleListing,
					buyerId,
					request.queueTokenJti(),
					LocalDateTime.now().plus(resaleHoldProperties.ttl())
				);

				resaleHoldRepository.save(resaleHold);

				return new ResaleHoldResponse(resaleHold.getId());
			});
	}

	@Transactional
	public ResaleReleaseResponse releaseResaleHold(UUID buyerId, UUID holdId) {
		ResaleHoldEntity resaleHold = resaleHoldRepository.findById(holdId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.RESALE_HOLD_NOT_FOUND)
			);
		Preconditions.validate(resaleHold.getUserId().equals(buyerId), ErrorCode.AUTH_PERMISSION_DENIED);

		String lockKey = buildLockKey(resaleHold.getResaleListing().getId());

		return distributedLockManager.withLock(
			lockKey,
			() -> {
				resaleHold.release();

				ResaleListingEntity resaleListing = resaleHold.getResaleListing();
				resaleListing.releaseHold();

				return new ResaleReleaseResponse(resaleHold.getId());
			}
		);
	}

	private static String buildLockKey(UUID listingId) {
		return "lock:resale:" + listingId;
	}
}
