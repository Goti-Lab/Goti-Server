package com.goti.resale.service.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.goti.constants.messages.ErrorCode;
import com.goti.dto.internal.ResaleOrderCreatedEvent;
import com.goti.dto.internal.ResaleOrderPaymentCompletedEvent;
import com.goti.dto.internal.SettlementCompletedEvent;
import com.goti.exception.CustomException;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;
import com.goti.resale.infra.TicketClient;
import com.goti.resale.repository.ResaleRestrictionRepository;
import com.goti.resale.repository.ResaleTransactionRepository;
import com.goti.resale.repository.history.ResalePriceHistoryRepository;
import com.goti.resale.repository.listing.ResaleListingRepository;
import com.goti.resale.service.infra.PaymentService;
import com.goti.resale.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResaleOrderEventListener {

	private final ResaleListingRepository listingRepository;
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

		ResaleRestrictionEntity restriction = restrictionService.getOrCreateRestriction(event.buyerId());

		for (ResaleTransactionEntity transaction : transactions) {
			ResaleListingEntity resaleListing = transaction.getListing();

			resaleListing.soldOut(transaction.getTransactionPrice());
			resaleListings.add(resaleListing);

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

		for (ResaleListingEntity listing : resaleListings) {
			transferOwnershipAsync(listing.getTicketId(), event.buyerId());
		}

		paymentService.releaseEscrow(event.resaleOrderId());
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleSettlementCompleted(SettlementCompletedEvent event) {
		log.info("정산 완료 이벤트 수신 및 처리: 주문ID {}", event.resaleOrderId());

		List<ResaleTransactionEntity> transactions = transactionRepository.findAllByResaleOrderId(event.resaleOrderId());

		List<ResaleListingEntity> listings = transactions.stream()
			.map(transaction -> {
				ResaleListingEntity listing = transaction.getListing();
				listing.settle();
				return listing;
			})
			.collect(Collectors.toList());

		listingRepository.saveAll(listings);
	}

	// TODO: 티켓이 나오면 구현
	@Async
	public void transferOwnershipAsync(UUID ticketId, UUID buyerId) {
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
