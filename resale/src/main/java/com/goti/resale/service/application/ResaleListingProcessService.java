package com.goti.resale.service.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.constants.ResaleSalesStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.dto.request.ResaleListingCancelRequest;
import com.goti.resale.dto.request.ResaleListingOrderCreateRequest;
import com.goti.resale.dto.response.ResaleListingMyPageCountResponse;
import com.goti.resale.dto.response.ResaleListingOrderCreateResponse;
import com.goti.resale.dto.response.ResaleListingResponse;
import com.goti.resale.repository.ResaleRestrictionRepository;
import com.goti.resale.repository.listing.ResaleListingRepository;
import com.goti.resale.service.domain.ResaleListingService;
import com.goti.resale.service.domain.ResaleRestrictionService;
import com.goti.resale.service.domain.command.ResaleSalesSearchCommand;
import com.goti.resale.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleListingProcessService {
	private final ResaleListingRepository listingRepository;
	private final ResaleRestrictionRepository restrictionRepository;
	private final ResaleRestrictionHandler restrictionHandler;
	private final ResaleRestrictionService restrictionService;
	private final ResaleListingService resaleListingService;

	@Transactional
	public ResaleListingOrderCreateResponse createListingOrder(UUID sellerId, ResaleListingOrderCreateRequest request) {
		return resaleListingService.createListingOrder(sellerId, request);
	}

	@Transactional
	public ResaleListingResponse cancelListing(UUID sellerId, ResaleListingCancelRequest request) {
		return resaleListingService.cancelListing(sellerId, request);
	}

	@Transactional
	public void cancelListingOrder(UUID sellerId, UUID listingOrderId) {
		resaleListingService.cancelListingOrder(sellerId, listingOrderId);
	}

	@Transactional(readOnly = true)
	public List<ResaleListingResponse> getListingsBySellerId(UUID sellerId) {
		List<ResaleListingEntity> resaleListings = listingRepository.findAllBySellerId(sellerId);

		return resaleListings.stream()
			.map(ResaleListingResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public Page<ResaleListingResponse> getMySales(ResaleSalesSearchCommand command) {
		List<ResaleListingStatus> targetStatuses = mapToStatuses(command.status());
		Pageable pageable = PageRequest.of(command.page(), command.size());

		return listingService.getMySales(
			command.sellerId(),
			targetStatuses,
			command.months(),
			command.startDate(),
			command.endDate(),
			pageable
		).map(ResaleListingResponse::from);
	}

	private List<ResaleListingStatus> mapToStatuses(ResaleSalesStatus status) {
		return switch (status) {
			case ALL -> null;
			case LISTING -> List.of(ResaleListingStatus.LISTING, ResaleListingStatus.HOLD);
			case PENDING -> List.of(ResaleListingStatus.SOLD);
			case SETTLED -> List.of(ResaleListingStatus.SETTLED);
			case CANCELED -> List.of(ResaleListingStatus.CANCELED);
		};
	}

	@Transactional(readOnly = true)
	public ResaleListingMyPageCountResponse getCountListings(UUID sellerId) {
		long listingCount = resaleListingService.countListings(sellerId);
		long soldCount = resaleListingService.countSold(sellerId);

		return new ResaleListingMyPageCountResponse(listingCount, soldCount);
	}

	@Transactional(readOnly = true)
	public long getListingCountByGrade(UUID gameId, UUID gradeId) {
		return listingRepository.countByGameIdAndGradeIdAndListingStatus(gameId, gradeId, ResaleListingStatus.LISTING);
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
