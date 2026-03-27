package com.goti.resale.service.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleListingOrderEntity;
import com.goti.resale.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;
import com.goti.resale.infra.TicketClient;
import com.goti.resale.infra.dto.ResaleOrderCreatedEvent;
import com.goti.resale.infra.dto.ResaleOrderPaymentCompletedEvent;
import com.goti.resale.infra.dto.SettlementCompletedEvent;
import com.goti.resale.repository.ResaleListingOrderRepository;
import com.goti.resale.repository.ResaleRestrictionRepository;
import com.goti.resale.repository.ResaleTransactionRepository;
import com.goti.resale.repository.history.ResalePriceHistoryRepository;
import com.goti.resale.repository.listing.ResaleListingRepository;
import com.goti.resale.repository.listingorder.ResaleListingOrderRepository;
import com.goti.resale.repository.transaction.ResaleTransactionRepository;
import com.goti.resale.service.domain.ResaleRestrictionService;
import com.goti.resale.service.infra.PaymentService;
import com.goti.resale.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResaleOrderEventListener {

	private final ResaleListingRepository listingRepository;
	private final ResaleListingOrderRepository listingOrderRepository;
	private final ResaleTransactionRepository transactionRepository;
	private final ResalePriceHistoryRepository priceHistoryRepository;
	private final ResaleRestrictionRepository restrictionRepository;
	private final ResaleRestrictionHandler restrictionHandler;
	private final ResaleRestrictionService restrictionService;
	private final PaymentService paymentService;
	private final TicketClient ticketClient;

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
		log.info("결제 완료 이벤트 수신: 주문ID {}", event.resaleOrderId());

		List<ResaleTransactionEntity> transactions = transactionRepository.findAllByResaleOrderId(event.resaleOrderId());

		List<ResaleListingEntity> resaleListings = new ArrayList<>();
		List<ResalePriceHistoryEntity> priceHistories = new ArrayList<>();
		Set<ResaleListingOrderEntity> listingOrders = new HashSet<>();

		ResaleRestrictionEntity restriction = restrictionService.getOrCreateRestriction(event.buyerId());

		for (ResaleTransactionEntity transaction : transactions) {
			ResaleListingEntity resaleListing = transaction.getListing();

			resaleListing.soldOut(transaction.getTransactionPrice());
			resaleListings.add(resaleListing);

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

		for (ResaleListingOrderEntity order : listingOrders) {
			List<ResaleListingEntity> allListings = listingRepository.findAllByListingOrderId(order.getId());
			boolean allCompleted = allListings.stream()
				.allMatch(l -> l.getListingStatus() == ResaleListingStatus.SOLD
					|| l.getListingStatus() == ResaleListingStatus.CANCELED);
			if (allCompleted) {
				order.soldOut();
			} else {
				order.partial();
			}
			listingOrderRepository.save(order);
		}

		for (ResaleListingEntity listing : resaleListings) {
			transferOwnership(listing.getTicketId(), event.buyerId());
		}

		paymentService.releaseEscrow(event.resaleOrderId());
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleSettlementCompleted(SettlementCompletedEvent event) {
		log.info("정산 완료 이벤트 수신 및 처리: 주문ID {}", event.resaleOrderId());

		List<ResaleTransactionEntity> transactions = transactionRepository.findAllByResaleOrderId(event.resaleOrderId());
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

		for (ResaleListingOrderEntity order : listingOrders) {
			List<ResaleListingEntity> allListings = listingRepository.findAllByListingOrderId(order.getId());
			boolean allSettled = allListings.stream()
				.allMatch(l -> l.getListingStatus() == ResaleListingStatus.SETTLED
					|| l.getListingStatus() == ResaleListingStatus.CANCELED);

			if (allSettled) {
				order.settled();
			} else {
				order.partial();
			}
			listingOrderRepository.save(order);
		}
	}

	@Async
	public void transferOwnership(UUID ticketId, UUID buyerId) {
		try {
			log.info("비동기 티켓 소유권 이전 시작 - 티켓ID: {}, 구매자: {}", ticketId, buyerId);
			ticketClient.transferOwnership(ticketId, buyerId);
		} catch (Exception e) {
			log.error("티켓 소유권 이전 실패 - 티켓ID: {}, 구매자: {}, 에러: {}",
				ticketId, buyerId, e.getMessage(), e);
			throw new CustomException(ErrorCode.TRANSFER_OWNERSHIP_FAILED);
		}
	}
}
