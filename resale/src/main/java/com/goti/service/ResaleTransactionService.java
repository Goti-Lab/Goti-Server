package com.goti.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.ResaleTransactionStatus;
import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.resale.ResaleListingEntity;
import com.goti.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.domain.entity.resale.ResaleTransactionEntity;
import com.goti.dto.request.ResaleTransactionRequest;
import com.goti.dto.response.ResaleTransactionInitResponse;
import com.goti.dto.response.ResaleTransactionSuccessResponse;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.repository.ResaleListingRepository;
import com.goti.repository.ResalePriceHistoryRepository;
import com.goti.repository.ResaleRestrictionRepository;
import com.goti.repository.ResaleTransactionRepository;
import com.goti.utils.ResalePricePolicy;
import com.goti.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ResaleTransactionService {
	private final ResaleListingRepository listingRepository;
	private final ResaleRestrictionRepository restrictionRepository;
	private final ResaleTransactionRepository transactionRepository;
	private final ResalePriceHistoryRepository priceHistoryRepository;
	private final ResaleRestrictionHandler restrictionHandler;
	private final ResalePricePolicy pricePolicy;
	private final PaymentService paymentService;

	@Transactional
	public ResaleTransactionInitResponse initTransaction(
		UUID buyerId,
		ResaleTransactionRequest request
	) {
		ResaleListingEntity listing = listingRepository.findById(request.listingId())
			.orElseThrow(() -> new CustomException(ErrorCode.LISTING_NOT_FOUND));

		Preconditions.validate(listing.isPurchasable(), ErrorCode.NOT_PURCHASABLE);

		ResaleRestrictionEntity restriction = getOrCreateRestriction(buyerId);
		restrictionHandler.validateCanBuy(restriction, listing.getGameId());

		ResalePricePolicy.FeeResult feeResult = pricePolicy.validateTransactionFee(
			listing.getListingPrice()
		);

		// TODO: Redisson 으로 TTL 구현
		try {
			listing.hold();
			listingRepository.saveAndFlush(listing);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		ResaleTransactionEntity transaction = ResaleTransactionEntity.create(
			listing,
			buyerId,
			listing.getSellerId(),
			listing.getListingPrice(),
			feeResult.buyerFee(),
			feeResult.sellerFee(),
			feeResult.buyerTotal(),
			feeResult.sellerTotal()
		);
		ResaleTransactionEntity saved = transactionRepository.save(transaction);

		ResaleTransactionInitResponse paymentResponse = paymentService.createResalePayment(
			listing.getId(),
			transaction.getId(),
			buyerId,
			feeResult.buyerTotal());

		//TODO : 결제 요청후 받는 이벤트

		log.info("리셀 완료 거래ID: {}, 구매자 비용: {}, 판매자 비용: {}"
			, saved.getId(), feeResult.buyerTotal(), feeResult.sellerTotal());

		return paymentResponse;
	}

	@Transactional
	public ResaleTransactionSuccessResponse completePayment(UUID transactionId, UUID escrowId) {
		ResaleTransactionEntity transaction = transactionRepository.findById(transactionId)
			.orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

		Preconditions.validate(transaction.getTransactionStatus() == ResaleTransactionStatus.PENDING,
			ErrorCode.NOT_MATCH_STATUS, "결제 대기");

		transaction.complete(escrowId);
		transactionRepository.save(transaction);

		ResaleListingEntity listing = transaction.getListing();
		listing.SoldOut(transaction.getTransactionPrice());
		listingRepository.save(listing);

		ResalePriceHistoryEntity priceHistory = ResalePriceHistoryEntity.create(
			listing.getGameId(),
			listing.getSeatId(),
			listing.getGradeId(),
			transaction.getTransactionPrice(),
			LocalDate.now(),
			LocalDateTime.now()
		);
		priceHistoryRepository.save(priceHistory);

		ResaleRestrictionEntity restriction = getOrCreateRestriction(transaction.getBuyerId());
		restrictionHandler.handleAfterBuy(restriction, listing.getGameId());

		restrictionRepository.save(restriction);

		publishTicketOwnershipTransfer(
			listing.getTicketId(),
			listing.getSellerId(),
			transaction.getBuyerId()
		);

		return ResaleTransactionSuccessResponse.result(transaction);
	}

	private void publishTicketOwnershipTransfer(UUID ticketId, UUID sellerId, UUID buyerId) {
		log.info("티켓ID: {} , 판매자ID: {} , 구매자ID: {}", ticketId, sellerId, buyerId);

		// TODO: 이벤트 Kafka? 를 이용하여 티켓 소유권 전달하기
	}

	private ResaleRestrictionEntity getOrCreateRestriction(UUID userId) {
		return restrictionRepository
			.findByUserId(userId)
			.orElseGet(() -> {
				ResaleRestrictionEntity newRestriction = ResaleRestrictionEntity.create(userId);
				return restrictionRepository.save(newRestriction);
			});
	}
}
