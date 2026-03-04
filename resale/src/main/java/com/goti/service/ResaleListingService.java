package com.goti.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.ResaleListingStatus;
import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.resale.ResaleListingEntity;
import com.goti.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.dto.request.ResaleListingCancelRequest;
import com.goti.dto.request.ResaleListingCreateRequest;
import com.goti.dto.response.ResaleListingResponse;
import com.goti.dto.response.ResaleTicketResponse;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.repository.ResaleListingRepository;
import com.goti.repository.ResaleRestrictionRepository;
import com.goti.utils.ResalePricePolicy;
import com.goti.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ResaleListingService {
	private final ResaleListingRepository listingRepository;
	private final ResaleRestrictionRepository restrictionRepository;
	private final ResaleRestrictionHandler restrictionHandler;
	private final ResalePricePolicy pricePolicy;
	private final TicketService ticketService;

	@Transactional
	public ResaleListingResponse createListing(UUID sellerId, ResaleListingCreateRequest request) {
		ResaleTicketResponse ticketInfo = ticketService.getTicketInfo(request.ticketId(), sellerId);

		validateTicketOwner(ticketInfo, sellerId);

		validateGameStartedOneHour(ticketInfo.gameDate());

		validateDuplicateListing(ticketInfo.ticketId());

		ResaleRestrictionEntity restriction = getOrCreateRestriction(sellerId);

		restrictionHandler.validateCanSell(restriction, ticketInfo.gameId());

		pricePolicy.validatePriceRange(ticketInfo.ticketPrice(), request.listingPrice());

		ResaleListingEntity listing = ResaleListingEntity.create(
			ticketInfo.ticketId(),
			sellerId,
			ticketInfo.gameId(),
			ticketInfo.seatInfo(),
			ticketInfo.ticketPrice(),
			request.listingPrice()
		);

		ResaleListingEntity saved = listingRepository.save(listing);

		restrictionHandler.handleAfterSell(restriction, ticketInfo.gameId());
		restrictionRepository.save(restriction);

		return ResaleListingResponse.from(saved);
	}

	@Transactional
	public ResaleListingResponse cancelListing(UUID sellerId, ResaleListingCancelRequest request) {
		ResaleListingEntity listing = listingRepository.findById(request.listingId())
			.orElseThrow(() -> new CustomException(ErrorCode.LISTING_NOT_FOUND));

		validateListingOwnership(listing, sellerId);

		validateCancelable(listing);

		ResaleRestrictionEntity restriction =
			restrictionRepository.findByUserId(sellerId)
				.orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,
					"정보를 찾을 수 없습니다"
				));

		restrictionHandler.validateCanCancel(restriction, listing.getGameId());

		listing.cancel();

		ResaleListingEntity saved = listingRepository.save(listing);

		restrictionHandler.handleAfterCancel(restriction, listing.getGameId());
		restrictionRepository.save(restriction);

		return ResaleListingResponse.from(saved);
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
				List.of(ResaleListingStatus.LISTING, ResaleListingStatus.HOLD)
			), ErrorCode.ALREADY_LISTED);
	}

	private void validateListingOwnership(ResaleListingEntity listing, UUID sellerId) {
		Preconditions.validate(
			listing.getSellerId().equals(sellerId),
			ErrorCode.AUTH_PERMISSION_DENIED,
			"본인의 리스팅만 취소할 수 있습니다"
		);
	}

	private void validateCancelable(ResaleListingEntity listing) {
		Preconditions.validate(
			listing.isCancelable(),
			ErrorCode.BAD_REQUEST,
			"취소할 수 없는 상태입니다 (현재: " + listing.getListingStatus() + ")"
		);
	}

	private ResaleRestrictionEntity getOrCreateRestriction(UUID userId) {
		return restrictionRepository
			.findByUserId(userId)
			.orElseGet(() -> {
				ResaleRestrictionEntity newRestriction = ResaleRestrictionEntity.create(userId);
				return restrictionRepository.save(newRestriction);
			});
	}
}
