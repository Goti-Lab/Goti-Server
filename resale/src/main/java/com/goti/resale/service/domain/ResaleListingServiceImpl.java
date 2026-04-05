package com.goti.resale.service.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.f4b6a3.tsid.TsidCreator;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.resale.constants.ResaleListingOrderStatus;
import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleListingOrderEntity;
import com.goti.resale.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.dto.request.ResaleListingCancelRequest;
import com.goti.resale.dto.request.ResaleListingCreateRequest;
import com.goti.resale.dto.request.ResaleListingOrderCreateRequest;
import com.goti.resale.dto.response.ResaleListingOrderCreateResponse;
import com.goti.resale.dto.response.ResaleListingResponse;
import com.goti.resale.dto.response.ResaleListingsCountResponse;
import com.goti.resale.dto.response.ResaleTicketResponse;
import com.goti.resale.infra.TicketClient;
import com.goti.resale.repository.ResaleRestrictionRepository;
import com.goti.resale.repository.history.ResalePriceHistoryRepository;
import com.goti.resale.repository.listing.ResaleListingRepository;
import com.goti.resale.repository.listingorder.ResaleListingOrderRepository;
import com.goti.resale.utils.ResalePricePolicy;
import com.goti.resale.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleListingServiceImpl implements ResaleListingService {

	private static final DateTimeFormatter ORDER_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
	private static final List<Integer> ALLOWED_MONTHS = List.of(1, 3, 6);
	private static final List<ResaleListingOrderStatus> ALLOWED_STATUSES = List.of(ResaleListingOrderStatus.LISTING,
		ResaleListingOrderStatus.PARTIAL);
	private static final List<ResaleListingStatus> DUPLICATE_STATUSES = List.of(ResaleListingStatus.LISTING,
		ResaleListingStatus.HOLD, ResaleListingStatus.SOLD);

	private final ResaleListingRepository listingRepository;
	private final ResaleListingOrderRepository listingOrderRepository;
	private final ResaleRestrictionRepository restrictionRepository;
	private final ResalePriceHistoryRepository priceHistoryRepository;
	private final ResaleRestrictionService restrictionService;
	private final ResaleRestrictionHandler restrictionHandler;
	private final ResalePricePolicy pricePolicy;
	private final TicketClient ticketClient;

	@Override
	public String generateListingOrderNumber() {
		String tsidSuffix = TsidCreator.getTsid().toString();
		return "ORD" + "-" +
			LocalDate.now().format(ORDER_NUMBER_FORMATTER) +
			tsidSuffix.substring(tsidSuffix.length() - 6);
	}

	@Override
	@Transactional
	public ResaleListingOrderCreateResponse createListingOrder(UUID sellerId, ResaleListingOrderCreateRequest request) {
		ResaleRestrictionEntity resaleRestriction = restrictionService.getOrCreateRestriction(sellerId);
		Map<UUID, ResaleListingOrderEntity> orderMap = new HashMap<>();
		List<ResaleListingEntity> listings = new ArrayList<>();

		for (ResaleListingCreateRequest listingRequest : request.listings()) {
			ResaleTicketResponse ticketInfo = ticketClient.getTicketInfo(listingRequest.ticketId(), sellerId);

			validateListingCreation(ticketInfo, sellerId, listingRequest.listingPrice(), resaleRestriction);

			ResaleListingOrderEntity listingOrder = orderMap.computeIfAbsent(ticketInfo.gradeId(), gradeId ->
				listingOrderRepository.findBySellerAndGrade(
					sellerId,
					gradeId,
					ALLOWED_STATUSES
				).orElseGet(() -> {
					ResaleListingOrderEntity order = ResaleListingOrderEntity.create(
						generateListingOrderNumber(),
						sellerId,
						gradeId
					);
					return listingOrderRepository.save(order);
				})
			);

			Integer lastTransactionPrice = priceHistoryRepository
				.findLatestByGameAndGrade(ticketInfo.gameId(), ticketInfo.gradeId())
				.map(ResalePriceHistoryEntity::getTransactionPrice)
				.orElse(null);

			ResaleListingEntity resaleListing = ResaleListingEntity.create(
				listingOrder,
				ticketInfo.ticketId(),
				sellerId,
				ticketInfo.gameId(),
				ticketInfo.seatId(),
				ticketInfo.sectionId(),
				ticketInfo.gradeId(),
				ticketInfo.seatInfo(),
				ticketInfo.ticketPrice(),
				listingRequest.listingPrice()
			);

			if (lastTransactionPrice != null) {
				resaleListing.initializeLastTransactionPrice(lastTransactionPrice);
			}

			listings.add(resaleListing);
			restrictionHandler.handleAfterSell(resaleRestriction, ticketInfo.gameId());
		}

		listingRepository.saveAll(listings);
		restrictionRepository.save(resaleRestriction);

		for (ResaleListingEntity listing : listings) {
			ticketClient.markAsResaleListing(listing.getTicketId(), sellerId);
		}

		List<ResaleListingResponse> listingResponses = listings.stream()
			.map(ResaleListingResponse::from)
			.toList();

		return ResaleListingOrderCreateResponse.from(orderMap.values(), listingResponses);
	}

	@Override
	@Transactional
	public ResaleListingResponse cancelListing(UUID sellerId, ResaleListingCancelRequest request) {
		ResaleListingEntity resaleListing = listingRepository.findById(request.listingId())
			.orElseThrow(
				() -> new CustomException(ErrorCode.LISTING_NOT_FOUND)
			);

		ResaleRestrictionEntity resaleRestriction = restrictionService.getOrCreateRestriction(sellerId);

		validateListingCancellation(
			sellerId,
			resaleListing,
			resaleRestriction
		);

		resaleListing.cancel();
		listingRepository.save(resaleListing);

		ticketClient.cancelResaleListing(resaleListing.getTicketId(), sellerId);

		ResaleListingOrderEntity order = resaleListing.getListingOrder();
		order.partial();

		List<ResaleListingEntity> listingsByOrderId = listingRepository.findAllByListingOrderId(order.getId());
		boolean allCancelled = listingsByOrderId.stream()
			.allMatch(l -> l.getListingStatus() == ResaleListingStatus.CANCELED);

		if (allCancelled) {
			order.cancel();
		}

		listingOrderRepository.save(order);

		restrictionHandler.handleAfterCancel(resaleRestriction, resaleListing.getGameId());
		restrictionRepository.save(resaleRestriction);

		return ResaleListingResponse.from(resaleListing);
	}

	@Override
	@Transactional
	public void cancelListingOrder(UUID sellerId, UUID orderId) {
		ResaleListingOrderEntity order = listingOrderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		Preconditions.validate(order.getSellerId().equals(sellerId), ErrorCode.AUTH_PERMISSION_DENIED);

		List<ResaleListingEntity> listings = listingRepository.findAllByListingOrderId(orderId);
		ResaleRestrictionEntity restriction = restrictionService.getOrCreateRestriction(sellerId);

		for (ResaleListingEntity listing : listings) {
			if (listing.isCancelable()) {
				validateListingCancellation(sellerId, listing, restriction);
				listing.cancel();
				ticketClient.cancelResaleListing(listing.getTicketId(), sellerId);
				restrictionHandler.handleAfterCancel(restriction, listing.getGameId());
			}
		}

		order.cancel();

		listingRepository.saveAll(listings);
		listingOrderRepository.save(order);
		restrictionRepository.save(restriction);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ResaleListingOrderEntity> getSalesHistory(
		UUID sellerId,
		List<ResaleListingOrderStatus> statuses,
		Integer months,
		LocalDate startDate,
		LocalDate endDate,
		Pageable pageable
	) {
		Preconditions.validate(
			sellerId != null,
			ErrorCode.AUTH_INVALID
		);
		validatePeriodFilter(months, startDate, endDate);

		return listingOrderRepository.getSalesHistory(sellerId, statuses, months, startDate, endDate, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public ResaleListingEntity getListing(UUID sellerId, UUID listingId) {
		ResaleListingEntity resaleListing = listingRepository.findById(listingId)
			.orElseThrow(() -> new CustomException(ErrorCode.LISTING_NOT_FOUND));

		validateListingOwnership(sellerId, resaleListing.getSellerId());

		return resaleListing;
	}

	@Override
	@Transactional(readOnly = true)
	public ResaleListingsCountResponse getResaleCount(UUID sellerId) {
		List<ResaleListingEntity> listings = listingRepository.findBySellerId(sellerId);

		long listingCount = listings.stream().filter(
				r ->
					r.getListingStatus() == ResaleListingStatus.LISTING ||
						r.getListingStatus() == ResaleListingStatus.HOLD)
			.count();

		long soldCount = listings.stream()
			.filter(r ->
				r.getListingStatus() == ResaleListingStatus.SOLD ||
					r.getListingStatus() == ResaleListingStatus.SETTLED)
			.count();

		return new ResaleListingsCountResponse(listingCount, soldCount);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ResaleListingEntity> getListingsByOrderId(UUID orderId) {
		return listingRepository.findAllByListingOrderId(orderId);
	}

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
				DUPLICATE_STATUSES
			), ErrorCode.ALREADY_LISTED);
	}

	private void validateListingOwnership(UUID sellerId, UUID listingSellerId) {
		Preconditions.validate(
			listingSellerId.equals(sellerId),
			ErrorCode.AUTH_PERMISSION_DENIED,
			"본인의 리셀만 취소할 수 있습니다"
		);
	}

	private void validatePeriodFilter(
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		Preconditions.validate(
			months == null || (startDate == null && endDate == null),
			ErrorCode.ORDER_HISTORY_PERIOD_FILTER_CONFLICT
		);

		Preconditions.validate(
			(startDate == null) == (endDate == null),
			ErrorCode.ORDER_HISTORY_PERIOD_DATE_REQUIRED
		);

		if (months != null) {
			Preconditions.validate(
				ALLOWED_MONTHS.contains(months),
				ErrorCode.ORDER_HISTORY_PERIOD_MONTHS_INVALID
			);
		}

		if (startDate != null && endDate != null) {
			Preconditions.validate(
				!startDate.isAfter(endDate),
				ErrorCode.ORDER_HISTORY_PERIOD_INVALID_RANGE
			);
		}
	}
}
