package com.goti.resale.service.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;
import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.dto.response.ResaleTicketResponse;
import com.goti.resale.repository.listing.ResaleListingRepository;
import com.goti.resale.utils.ResalePricePolicy;
import com.goti.resale.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleListingDomainServiceImpl implements ResaleListingDomainService {

	private final ResaleListingRepository listingRepository;
	private final ResaleRestrictionHandler restrictionHandler;
	private final ResalePricePolicy pricePolicy;

	@Override
	public void validateListingCreation(
		ResaleTicketResponse ticketInfo,
		UUID sellerId,
		Integer listingPrice,
		ResaleRestrictionEntity restriction
	) {
		validateTicketOwner(ticketInfo, sellerId);
		validateGameStartedOneHour(ticketInfo.gameDate());
		validateDuplicateListing(ticketInfo.ticketId());
		restrictionHandler.validateReListingLimit(ticketInfo.transactionId(), ticketInfo.createdAt());
		restrictionHandler.validateCanSell(restriction, ticketInfo.gameId());
		pricePolicy.validatePriceRange(ticketInfo.ticketPrice(), listingPrice);
	}

	@Override
	public void validateListingCancellation(
		UUID sellerId,
		ResaleListingEntity resaleListing,
		ResaleRestrictionEntity resaleRestriction
	) {
		validateListingOwnership(sellerId, resaleListing.getSellerId());
		restrictionHandler.validateCanCancel(resaleRestriction, resaleListing.getGameId());
	}

	private void validateTicketOwner(ResaleTicketResponse ticketResponse, UUID sellerId) {
		Preconditions.validate(ticketResponse.ownerId().equals(sellerId), ErrorCode.AUTH_PERMISSION_DENIED);
	}

	private void validateGameStartedOneHour(LocalDateTime gameDate) {
		LocalDateTime now = LocalDateTime.now();
		Preconditions.validate(gameDate.isAfter(now.plusHours(1)), ErrorCode.LISTING_ALREADY_CLOSED);
	}

	private void validateDuplicateListing(UUID ticketId) {
		Preconditions.validate(
			!listingRepository.existsByTicketIdAndListingStatusIn(
				ticketId,
				List.of(ResaleListingStatus.LISTING, ResaleListingStatus.HOLD, ResaleListingStatus.SOLD)
			), ErrorCode.ALREADY_LISTED);
	}

	private void validateListingOwnership(UUID sellerId, UUID listingSellerId) {
		Preconditions.validate(
			listingSellerId.equals(sellerId),
			ErrorCode.AUTH_PERMISSION_DENIED,
			"본인의 리셀만 취소할 수 있습니다"
		);
	}
}
