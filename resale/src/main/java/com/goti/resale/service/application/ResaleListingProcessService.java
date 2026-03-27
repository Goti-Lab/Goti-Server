package com.goti.resale.service.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.dto.request.ResaleListingCancelRequest;
import com.goti.resale.dto.request.ResaleListingOrderCreateRequest;
import com.goti.resale.dto.response.ResaleListingOrderCreateResponse;
import com.goti.resale.dto.response.ResaleListingResponse;
import com.goti.resale.repository.listing.ResaleListingRepository;
import com.goti.resale.service.domain.ListingService;
import com.goti.resale.service.domain.ResaleRestrictionService;
import com.goti.resale.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleListingService {
	private final ResaleListingRepository listingRepository;
	private final com.goti.resale.repository.ResaleRestrictionRepository restrictionRepository;
	private final ResaleRestrictionHandler restrictionHandler;
	private final ResaleRestrictionService restrictionService;
	private final ListingService listingService;

	@Transactional
	public ResaleListingOrderCreateResponse createListingOrder(UUID sellerId, ResaleListingOrderCreateRequest request) {
		return listingService.createListingOrder(sellerId, request);
	}

	@Transactional
	public ResaleListingResponse cancelListing(UUID sellerId, ResaleListingCancelRequest request) {
		return listingService.cancelListing(sellerId, request);
	}

	@Transactional
	public void cancelListingOrder(UUID sellerId, UUID listingOrderId) {
		listingService.cancelListingOrder(sellerId, listingOrderId);
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
