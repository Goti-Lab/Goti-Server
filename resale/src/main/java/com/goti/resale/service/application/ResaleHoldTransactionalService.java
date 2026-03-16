package com.goti.resale.service.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.resale.config.properties.ResaleHoldProperties;
import com.goti.constants.messages.ErrorCode;
import com.goti.resale.domain.entity.resale.ResaleHoldEntity;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.dto.request.ResaleHoldRequest;
import com.goti.resale.dto.response.ResaleHoldResponse;
import com.goti.resale.dto.response.ResaleReleaseResponse;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.resale.repository.hold.ResaleHoldRepository;
import com.goti.resale.repository.listing.ResaleListingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleHoldTransactionalService {
	private final ResaleListingRepository resaleListingRepository;
	private final ResaleHoldRepository resaleHoldRepository;
	private final ResaleHoldProperties resaleHoldProperties;

	@Transactional
	public ResaleHoldResponse hold(UUID buyerId, ResaleHoldRequest request) {
		ResaleListingEntity resaleListing = resaleListingRepository.findById(request.listingId())
			.orElseThrow(
				() -> new CustomException(ErrorCode.LISTING_NOT_FOUND)
			);

		Preconditions.validate(resaleListing.isPurchasable(), ErrorCode.NOT_PURCHASABLE);

		Preconditions.validate(
			!resaleListing.getSellerId().equals(buyerId),
			ErrorCode.NOT_PURCHASABLE_SELF
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
	}

	@Transactional
	public ResaleReleaseResponse release(UUID holdId) {
		ResaleHoldEntity resaleHold = resaleHoldRepository.findById(holdId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.RESALE_HOLD_NOT_FOUND)
			);
		resaleHold.release();

		ResaleListingEntity resaleListing = resaleHold.getResaleListing();
		resaleListing.releaseHold();

		return new ResaleReleaseResponse(resaleHold.getId());
	}
}
