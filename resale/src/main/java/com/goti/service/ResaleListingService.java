package com.goti.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.ResaleListingStatus;
import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.resale.ResaleListingEntity;
import com.goti.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.dto.request.ResaleListingCancelRequest;
import com.goti.dto.request.ResaleListingCreateRequest;
import com.goti.dto.response.ResaleListingResponse;
import com.goti.dto.response.ResaleTicketResponse;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.repository.ResaleRestrictionRepository;
import com.goti.repository.history.ResalePriceHistoryRepository;
import com.goti.repository.listing.ResaleListingRepository;
import com.goti.utils.ResalePricePolicy;
import com.goti.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleListingService {
	private final ResaleListingRepository listingRepository;
	private final ResaleRestrictionRepository restrictionRepository;
	private final ResalePriceHistoryRepository priceHistoryRepository;
	private final ResaleRestrictionHandler restrictionHandler;
	private final ResalePricePolicy pricePolicy;
	private final TicketService ticketService;

	@Transactional
	public ResaleListingResponse createListing(UUID sellerId, ResaleListingCreateRequest request) {
		ResaleTicketResponse ticketInfo = ticketService.getTicketInfo(request.ticketId(), sellerId);

		validateTicketOwner(ticketInfo, sellerId);

		validateGameStartedOneHour(ticketInfo.gameDate());

		validateDuplicateListing(ticketInfo.ticketId());

		ResaleRestrictionEntity resaleRestriction = getOrCreateRestriction(sellerId);

		restrictionHandler.validateCanSell(resaleRestriction, ticketInfo.gameId());

		pricePolicy.validatePriceRange(ticketInfo.ticketPrice(), request.listingPrice());

		Integer lastTransactionPrice = priceHistoryRepository
			.findByGameAndGrade(ticketInfo.gameId(), ticketInfo.gradeId())
			.map(ResalePriceHistoryEntity::getTransactionPrice)
			.orElse(null);

		ResaleListingEntity resaleListing = ResaleListingEntity.create(
			ticketInfo.ticketId(),
			sellerId,
			ticketInfo.gameId(),
			ticketInfo.seatId(),
			ticketInfo.gradeId(),
			ticketInfo.seatInfo(),
			ticketInfo.ticketPrice(),
			request.listingPrice()
		);

		if (lastTransactionPrice != null) {
			resaleListing.initializeLastTransactionPrice(lastTransactionPrice);
		}

		listingRepository.save(resaleListing);

		restrictionHandler.handleAfterSell(resaleRestriction, ticketInfo.gameId());
		restrictionRepository.save(resaleRestriction);

		return ResaleListingResponse.from(resaleListing);
	}

	@Transactional
	public ResaleListingResponse cancelListing(UUID sellerId, ResaleListingCancelRequest request) {
		ResaleListingEntity resaleListing = listingRepository.findById(request.listingId())
			.orElseThrow(
				() -> new CustomException(ErrorCode.LISTING_NOT_FOUND)
			);

		validateListingOwnership(resaleListing, sellerId);

		validateCancelable(resaleListing);

		ResaleRestrictionEntity resaleRestriction =
			restrictionRepository.findByUserId(sellerId)
				.orElseThrow(
					() -> new CustomException(ErrorCode.SELLER_NOT_FOUND)
				);

		restrictionHandler.validateCanCancel(resaleRestriction, resaleListing.getGameId());

		resaleListing.cancel();

		listingRepository.save(resaleListing);

		restrictionHandler.handleAfterCancel(resaleRestriction, resaleListing.getGameId());
		restrictionRepository.save(resaleRestriction);

		return ResaleListingResponse.from(resaleListing);
	}

	@Transactional
	public List<ResaleListingResponse> getListingsBySellerId(UUID sellerId) {
		List<ResaleListingEntity> resaleListings = listingRepository.findAllBySellerId(sellerId);

		return resaleListings.stream()
			.map(ResaleListingResponse::from)
			.toList();
	}

	@Transactional
	public void cancelListingCauseGameStart(UUID gameId) {
		List<ResaleListingEntity> resaleListings = listingRepository.findByGameAndStatusIn(
			gameId,
			List.of(ResaleListingStatus.LISTING, ResaleListingStatus.HOLD)
		);

		for (ResaleListingEntity listing : resaleListings) {
			listing.cancelByGameStart();
			listingRepository.save(listing);

			ResaleRestrictionEntity resaleRestriction = getOrCreateRestriction(listing.getSellerId());

			restrictionHandler.handleAfterCancel(resaleRestriction, listing.getGameId());
			restrictionRepository.save(resaleRestriction);
		}
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
			!listingRepository.existsByTicketAndStatusIn(
				ticketId,
				List.of(ResaleListingStatus.LISTING, ResaleListingStatus.HOLD, ResaleListingStatus.SOLD)
			), ErrorCode.ALREADY_LISTED);
	}

	private void validateListingOwnership(ResaleListingEntity listing, UUID sellerId) {
		Preconditions.validate(
			listing.getSellerId().equals(sellerId),
			ErrorCode.AUTH_PERMISSION_DENIED,
			"본인의 리셀만 취소할 수 있습니다"
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
