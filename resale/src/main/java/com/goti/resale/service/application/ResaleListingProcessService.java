package com.goti.resale.service.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.dto.request.ResaleListingCancelRequest;
import com.goti.resale.dto.request.ResaleListingCreateRequest;
import com.goti.resale.dto.response.ResaleListingResponse;
import com.goti.resale.dto.response.ResaleTicketResponse;
import com.goti.resale.infra.TicketClient;
import com.goti.resale.repository.ResaleRestrictionRepository;
import com.goti.resale.repository.history.ResalePriceHistoryRepository;
import com.goti.resale.repository.listing.ResaleListingRepository;
import com.goti.resale.service.domain.ResaleListingService;
import com.goti.resale.service.domain.ResaleRestrictionService;
import com.goti.resale.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleListingProcessService {
	private final ResaleListingRepository listingRepository;
	private final ResaleRestrictionRepository restrictionRepository;
	private final ResalePriceHistoryRepository priceHistoryRepository;
	private final ResaleRestrictionHandler restrictionHandler;
	private final ResaleRestrictionService restrictionService;
	private final ResaleListingService resaleListingService;
	private final TicketClient ticketClient;

	@Transactional
	public ResaleListingResponse createListing(UUID sellerId, ResaleListingCreateRequest request) {
		ResaleTicketResponse ticketInfo = ticketClient.getTicketInfo(request.ticketId(), sellerId);

		ResaleRestrictionEntity resaleRestriction = restrictionService.getOrCreateRestriction(sellerId);

		resaleListingService.validateListingCreation(ticketInfo, sellerId, request.listingPrice(), resaleRestriction);

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

		ResaleRestrictionEntity resaleRestriction = restrictionService.getOrCreateRestriction(sellerId);

		resaleListingService.validateListingCancellation(
			sellerId,
			resaleListing,
			resaleRestriction
		);

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

		Map<UUID, ResaleRestrictionEntity> restrictionMap = restrictionService.getOrCreateRestrictions(sellerIds);

		for (ResaleListingEntity listing : listings) {
			listing.cancelByGameStart();

			ResaleRestrictionEntity restriction = restrictionMap.get(listing.getSellerId());

			restrictionHandler.handleAfterCancel(restriction, listing.getGameId());
		}

		listingRepository.saveAll(listings);
		restrictionRepository.saveAll(restrictionMap.values());
	}
}