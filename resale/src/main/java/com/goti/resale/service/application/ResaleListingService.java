package com.goti.resale.service.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.resale.constants.ResaleListingStatus;
import com.goti.constants.messages.ErrorCode;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.dto.request.ResaleListingCancelRequest;
import com.goti.resale.dto.request.ResaleListingCreateRequest;
import com.goti.resale.dto.response.ResaleListingResponse;
import com.goti.resale.dto.response.ResaleTicketResponse;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.resale.infra.TicketClient;
import com.goti.resale.repository.ResaleRestrictionRepository;
import com.goti.resale.repository.history.ResalePriceHistoryRepository;
import com.goti.resale.repository.listing.ResaleListingRepository;
import com.goti.resale.utils.ResalePricePolicy;
import com.goti.resale.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleListingService {
	private final ResaleListingRepository listingRepository;
	private final ResaleRestrictionRepository restrictionRepository;
	private final ResalePriceHistoryRepository priceHistoryRepository;
	private final ResaleRestrictionHandler restrictionHandler;
	private final ResaleRestrictionService restrictionService;
	private final ResalePricePolicy pricePolicy;
	private final TicketClient ticketClient;

	@Transactional
	public ResaleListingResponse createListing(UUID sellerId, ResaleListingCreateRequest request) {
		ResaleTicketResponse ticketInfo = ticketClient.getTicketInfo(request.ticketId(), sellerId);

		validateTicketOwner(ticketInfo, sellerId);

		validateGameStartedOneHour(ticketInfo.gameDate());

		validateDuplicateListing(ticketInfo.ticketId());

		restrictionHandler.validateReListingLimit(ticketInfo.transactionId(), ticketInfo.createdAt());

		ResaleRestrictionEntity resaleRestriction = restrictionService.getOrCreateRestriction(sellerId);

		restrictionHandler.validateCanSell(resaleRestriction, ticketInfo.gameId());

		pricePolicy.validatePriceRange(ticketInfo.ticketPrice(), request.listingPrice());

		Integer lastTransactionPrice = priceHistoryRepository
			.findLatestByGameAndGrade(ticketInfo.gameId(), ticketInfo.gradeId())
			.map(ResalePriceHistoryEntity::getTransactionPrice)
			.orElse(null);

		ResaleListingEntity resaleListing = ResaleListingEntity.create(
			ticketInfo.ticketId(),
			sellerId,
			ticketInfo.gameId(),
			ticketInfo.seatId(),
			ticketInfo.sectionId(),
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

		ResaleRestrictionEntity resaleRestriction = restrictionService.getOrCreateRestriction(sellerId);

		restrictionHandler.validateCanCancel(resaleRestriction, resaleListing.getGameId());

		resaleListing.cancel();

		listingRepository.save(resaleListing);

		restrictionHandler.handleAfterCancel(resaleRestriction, resaleListing.getGameId());
		restrictionRepository.save(resaleRestriction);

		return ResaleListingResponse.from(resaleListing);
	}

	@Transactional(readOnly = true)
	public List<ResaleListingResponse> getListingsBySellerId(UUID sellerId) {
		List<ResaleListingEntity> resaleListings = listingRepository.findAllBySellerId(sellerId);

		return resaleListings.stream()
			.map(ResaleListingResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public long getListingCountBySection(UUID gameId, UUID sectionId) {
		return listingRepository.countByGameIdAndSectionIdAndListingStatus(gameId, sectionId, ResaleListingStatus.LISTING);
	}

	@Transactional(readOnly = true)
	public long getTotalListingCount(UUID gameId) {
		return listingRepository.countByGameIdAndListingStatus(gameId, ResaleListingStatus.LISTING);
	}

	@Transactional
	public void cancelListingsByGameIds(List<UUID> gameIds) {
		if (gameIds == null || gameIds.isEmpty()) {
			return;
		}

		List<ResaleListingEntity> listings = listingRepository.findByGameIdInAndListingStatusIn(
			gameIds,
			List.of(ResaleListingStatus.LISTING, ResaleListingStatus.HOLD)
		);

		if (listings.isEmpty()) {
			return;
		}

		List<UUID> sellerIds = listings.stream()
			.map(ResaleListingEntity::getSellerId)
			.distinct()
			.toList();

		Map<UUID, ResaleRestrictionEntity> restrictionMap = sellerIds.stream()
			.collect(Collectors.toMap(id -> id, restrictionService::getOrCreateRestriction));

		for (ResaleListingEntity listing : listings) {
			listing.cancelByGameStart();

			ResaleRestrictionEntity restriction = restrictionMap.get(listing.getSellerId());

			restrictionHandler.handleAfterCancel(restriction, listing.getGameId());
		}

		listingRepository.saveAll(listings);
		restrictionRepository.saveAll(restrictionMap.values());
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
}