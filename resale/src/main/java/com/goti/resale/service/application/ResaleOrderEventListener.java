package com.goti.resale.service.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.goti.dto.internal.ResaleOrderCreatedEvent;
import com.goti.dto.internal.ResaleOrderPaymentCompletedEvent;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;
import com.goti.resale.infra.TicketClient;
import com.goti.resale.repository.ResaleRestrictionRepository;
import com.goti.resale.repository.ResaleTransactionRepository;
import com.goti.resale.repository.history.ResalePriceHistoryRepository;
import com.goti.resale.repository.listing.ResaleListingRepository;
import com.goti.resale.utils.ResaleRestrictionHandler;
import com.goti.service.infra.PaymentService;

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
			event.paymentItems()
		);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handlePaymentCompleted(ResaleOrderPaymentCompletedEvent event) {
		log.info("결제 완료 이벤트 수신: 주문ID {}", event.resaleOrderId());

		List<ResaleTransactionEntity> transactions = transactionRepository.findAllByResaleOrderId(event.resaleOrderId());

		List<ResaleListingEntity> listingsToUpdate = new ArrayList<>();
		List<ResalePriceHistoryEntity> priceHistories = new ArrayList<>();

		for (ResaleTransactionEntity transaction : transactions) {
			ResaleListingEntity listing = transaction.getListing();

			listing.SoldOut(transaction.getTransactionPrice());
			listingsToUpdate.add(listing);

			priceHistories.add(
				ResalePriceHistoryEntity.create(
					listing.getGameId(),
					listing.getSeatId(),
					listing.getSectionId(),
					listing.getGradeId(),
					transaction.getTransactionPrice()
				));

			handleBuyerRestriction(event.buyerId(), listing.getGameId());

			log.info("티켓 소유권 이전 처리 티켓ID: {}, 구매자: {}", listing.getTicketId(), event.buyerId());
			ticketClient.transferOwnership(listing.getTicketId(), event.buyerId());
		}

		listingRepository.saveAll(listingsToUpdate);
		priceHistoryRepository.saveAll(priceHistories);

		paymentService.releaseEscrow(event.resaleOrderId());
	}

	private void handleBuyerRestriction(UUID buyerId, UUID gameId) {
		ResaleRestrictionEntity restriction = restrictionRepository.findByUserId(buyerId)
			.orElseGet(() -> {
				ResaleRestrictionEntity newRestriction = ResaleRestrictionEntity.create(buyerId);
				return restrictionRepository.save(newRestriction);
			});
		restrictionHandler.handleAfterBuy(restriction, gameId);
		restrictionRepository.save(restriction);
	}
}
