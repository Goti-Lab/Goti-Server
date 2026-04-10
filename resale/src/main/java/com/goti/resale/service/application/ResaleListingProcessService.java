package com.goti.resale.service.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.global.dto.Paging;
import com.goti.resale.constants.ResaleListingOrderStatus;
import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.constants.ResaleOrderSearchStatus;
import com.goti.resale.constants.ResaleStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.dto.request.ResaleListingCancelRequest;
import com.goti.resale.dto.request.ResaleListingOrderCreateRequest;
import com.goti.resale.dto.response.ResaleListingOrderCreateResponse;
import com.goti.resale.dto.response.ResaleListingOrderResponse;
import com.goti.resale.dto.response.ResaleListingResponse;
import com.goti.resale.dto.response.ResaleListingsCountResponse;
import com.goti.resale.dto.response.ResaleStatusResponse;
import com.goti.resale.repository.ResaleRestrictionRepository;
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
	public ResaleListingResponse getListing(UUID sellerId, UUID listingId) {
		ResaleListingEntity resaleListing = resaleListingService.getListing(sellerId, listingId);
		return ResaleListingResponse.from(resaleListing);
	}

	@Transactional(readOnly = true)
	public List<ResaleListingResponse> getSalesDetails(UUID sellerId, UUID orderId) {
		return resaleListingService.getListingsByOrderId(orderId).stream()
			.filter(listing -> listing.getSellerId().equals(sellerId))
			.map(ResaleListingResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public Page<ResaleListingOrderResponse> getSalesHistory(UUID sellerId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate,
		ResaleOrderSearchStatus status,
		Paging paging
	) {
		List<ResaleListingOrderStatus> targetStatuses = mapToStatuses(status);
		return resaleListingService.getSalesHistory(
			sellerId,
			targetStatuses,
			months,
			startDate,
			endDate,
			paging.toPageable()
		);
	}

	private List<ResaleListingOrderStatus> mapToStatuses(ResaleOrderSearchStatus status) {
		if (status == null) {
			return null;
		}
		return switch (status) {
			case ALL -> null;
			case LISTING -> List.of(ResaleListingOrderStatus.LISTING, ResaleListingOrderStatus.PARTIAL);
			case PENDING -> List.of(ResaleListingOrderStatus.SOLD);
			case SETTLED -> List.of(ResaleListingOrderStatus.SETTLED);
			case CANCELED -> List.of(ResaleListingOrderStatus.CANCELED);
		};
	}

	@Transactional(readOnly = true)
	public ResaleListingsCountResponse getResaleCount(UUID sellerId) {
		return resaleListingService.getResaleCount(sellerId);
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

	@Transactional(readOnly = true)
	public ResaleStatusResponse getResaleStatus(UUID gameId) {
		ResaleStatus status = resaleListingService.getResaleStatus(gameId);
		return new ResaleStatusResponse(status);
	}
}
