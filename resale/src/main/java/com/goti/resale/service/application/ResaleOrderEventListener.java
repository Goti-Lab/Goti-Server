package com.goti.resale.service.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleListingOrderEntity;
import com.goti.resale.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;
import com.goti.resale.infra.dto.ResaleOrderCreatedEvent;
import com.goti.resale.infra.dto.ResaleOrderPaymentCompletedEvent;
import com.goti.resale.infra.dto.SettlementCompletedEvent;
import com.goti.resale.infra.dto.TicketOwnershipTransferEvent;
import com.goti.resale.repository.ResaleRestrictionRepository;
import com.goti.resale.repository.history.ResalePriceHistoryRepository;
import com.goti.resale.repository.listing.ResaleListingRepository;
import com.goti.resale.repository.listingorder.ResaleListingOrderRepository;
import com.goti.resale.service.domain.ResaleListingService;
import com.goti.resale.service.domain.ResaleOrderService;
import com.goti.resale.service.domain.ResaleRestrictionService;
import com.goti.resale.service.infra.PaymentService;
import com.goti.resale.utils.ResaleRestrictionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResaleOrderEventListener {

	private final ResaleListingRepository listingRepository;
	private final ResaleListingOrderRepository listingOrderRepository;
	private final ResalePriceHistoryRepository priceHistoryRepository;
	private final ResaleRestrictionRepository restrictionRepository;
	private final ResaleRestrictionHandler restrictionHandler;
	private final ResaleRestrictionService restrictionService;
	private final ResaleListingService listingService;
	private final ResaleOrderService orderService;
	private final PaymentService paymentService;
	private final ApplicationEventPublisher eventPublisher;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleOrderCreated(ResaleOrderCreatedEvent event) {
		paymentService.createResalePayment(
			event.orderId(),
			event.buyerId(),
			event.totalBuyerAmount(),
			event.totalBuyerFee(),
			event.totalSellerFee(),
			event.paymentItems(),
			event.idempotencyKey()
		);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handlePaymentCompleted(ResaleOrderPaymentCompletedEvent event) {
		log.info("=== 결제 완료 이벤트 수신: 주문ID {} ===", event.resaleOrderId());

		try {
			List<ResaleTransactionEntity> transactions = orderService.findTransactionByOrder(
				event.resaleOrderId());

			List<UUID> listingIds = transactions.stream()
				.map(t -> t.getListing().getId())
				.toList();

			List<ResaleListingEntity> resaleListings = listingRepository.findAllById(listingIds);

			Map<UUID, ResaleListingEntity> listingMap = resaleListings.stream()
				.collect(Collectors.toMap(ResaleListingEntity::getId, Function.identity()));

			List<ResalePriceHistoryEntity> priceHistories = new ArrayList<>();
			Set<ResaleListingOrderEntity> listingOrders = new HashSet<>();

			ResaleRestrictionEntity restriction = restrictionService.getOrCreateRestriction(event.buyerId());

			for (ResaleTransactionEntity transaction : transactions) {
				ResaleListingEntity resaleListing = listingMap.get(transaction.getListing().getId());

				if (resaleListing == null) {
					log.error("❌ Listing을 찾을 수 없음: {}", transaction.getListing().getId());
					continue;
				}

				resaleListing.soldOut(transaction.getTransactionPrice());

				listingOrders.add(resaleListing.getListingOrder());

				BigDecimal basePrice = BigDecimal.valueOf(resaleListing.getDailyBasePrice());
				BigDecimal transactionPrice = BigDecimal.valueOf(transaction.getTransactionPrice());
				BigDecimal changePercent = transactionPrice.subtract(basePrice)
					.divide(basePrice, 2, RoundingMode.HALF_UP)
					.multiply(BigDecimal.valueOf(100));

				priceHistories.add(
					ResalePriceHistoryEntity.create(
						resaleListing.getGameId(),
						resaleListing.getSeatId(),
						resaleListing.getSectionId(),
						resaleListing.getGradeId(),
						transaction.getTransactionPrice(),
						changePercent
					));

				restrictionHandler.handleAfterBuy(restriction, resaleListing.getGameId());
			}

			listingRepository.saveAll(resaleListings);
			priceHistoryRepository.saveAll(priceHistories);
			restrictionRepository.save(restriction);

			listingService.updateListingOrders(listingOrders);

			paymentService.releaseEscrow(event.resaleOrderId());

			String authToken = extractCurrentToken();
			TicketOwnershipTransferEvent transferEvent = TicketOwnershipTransferEvent.from(event, authToken);
			eventPublisher.publishEvent(transferEvent);

		} catch (Exception e) {
			log.error("❌ 결제 완료 이벤트 처리 실패 - 주문ID: {}", event.resaleOrderId(), e);
			throw e;
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleSettlementCompleted(SettlementCompletedEvent event) {
		log.info("정산 완료 이벤트 수신 및 처리: 주문ID {}", event.resaleOrderId());

		List<ResaleTransactionEntity> transactions = orderService.findTransactionByOrder(
			event.resaleOrderId());
		Set<ResaleListingOrderEntity> listingOrders = new HashSet<>();

		List<ResaleListingEntity> listings = transactions.stream()
			.map(transaction -> {
				ResaleListingEntity listing = transaction.getListing();
				listing.settle();
				listingOrders.add(listing.getListingOrder());
				return listing;
			})
			.toList();

		listingRepository.saveAll(listings);

		List<UUID> listingOrderIds = listingOrders.stream()
			.map(ResaleListingOrderEntity::getId)
			.toList();

		Map<UUID, List<ResaleListingEntity>> listingsByOrderId = listingRepository.findAllByListingOrderIdIn(
				listingOrderIds)
			.stream()
			.collect(Collectors.groupingBy(l -> l.getListingOrder().getId()));

		for (ResaleListingOrderEntity order : listingOrders) {
			List<ResaleListingEntity> allListings = listingsByOrderId.getOrDefault(order.getId(), List.of());
			boolean allSettled = allListings.stream()
				.allMatch(l -> l.getListingStatus() == ResaleListingStatus.SETTLED
					|| l.getListingStatus() == ResaleListingStatus.CANCELED);

			if (allSettled) {
				order.settled();
			} else {
				order.partial();
			}
		}
		listingOrderRepository.saveAll(listingOrders);
	}

	private String extractCurrentToken() {
		ServletRequestAttributes attrs =
			(ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if (attrs != null) {
			HttpServletRequest request = attrs.getRequest();
			String bearer = request.getHeader("Authorization");
			if (bearer != null && bearer.startsWith("Bearer ")) {
				return bearer.substring(7);
			}
		}
		log.warn("⚠️ 현재 요청에서 인증 토큰을 찾을 수 없습니다.");
		return null;
	}
}