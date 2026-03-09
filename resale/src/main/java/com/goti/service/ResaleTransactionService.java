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
		ResaleListingEntity resaleListing = listingRepository.findById(request.listingId())
			.orElseThrow(() -> new CustomException(ErrorCode.LISTING_NOT_FOUND));

		Preconditions.validate(resaleListing.isPurchasable(), ErrorCode.NOT_PURCHASABLE);

		ResaleRestrictionEntity resaleRestriction = getOrCreateRestriction(buyerId);
		restrictionHandler.validateCanBuy(resaleRestriction, resaleListing.getGameId());

		ResalePricePolicy.FeeResult feeResult = pricePolicy.validateTransactionFee(
			resaleListing.getListingPrice()
		);

		// TODO: Redisson 으로 TTL 구현
		try {
			resaleListing.hold();
			listingRepository.saveAndFlush(resaleListing);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		ResaleTransactionEntity transaction = ResaleTransactionEntity.create(
			resaleListing,
			buyerId,
			resaleListing.getSellerId(),
			resaleListing.getListingPrice(),
			feeResult.buyerFee(),
			feeResult.sellerFee(),
			feeResult.buyerTotal(),
			feeResult.sellerTotal()
		);
		ResaleTransactionEntity saved = transactionRepository.save(transaction);

		ResaleTransactionInitResponse paymentResponse = paymentService.createResalePayment(
			resaleListing.getId(),
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
		ResaleTransactionEntity resaleTransaction = transactionRepository.findById(transactionId)
			.orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

		Preconditions.validate(resaleTransaction.getTransactionStatus() == ResaleTransactionStatus.PENDING,
			ErrorCode.NOT_MATCH_STATUS, "결제 대기");

		resaleTransaction.complete(escrowId);
		transactionRepository.save(resaleTransaction);

		ResaleListingEntity resaleListing = resaleTransaction.getListing();
		resaleListing.SoldOut(resaleTransaction.getTransactionPrice());
		listingRepository.save(resaleListing);

		ResalePriceHistoryEntity resalePriceHistory = ResalePriceHistoryEntity.create(
			resaleListing.getGameId(),
			resaleListing.getSeatId(),
			resaleListing.getGradeId(),
			resaleTransaction.getTransactionPrice(),
			LocalDate.now(),
			LocalDateTime.now()
		);
		priceHistoryRepository.save(resalePriceHistory);

		ResaleRestrictionEntity resaleRestriction = getOrCreateRestriction(resaleTransaction.getBuyerId());
		restrictionHandler.handleAfterBuy(resaleRestriction, resaleListing.getGameId());

		restrictionRepository.save(resaleRestriction);

		publishTicketOwnershipTransfer(
			resaleListing.getTicketId(),
			resaleListing.getSellerId(),
			resaleTransaction.getBuyerId()
		);

		return ResaleTransactionSuccessResponse.result(resaleTransaction);
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
