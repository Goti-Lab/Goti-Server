package com.goti.service.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.ResaleHoldStatus;
import com.goti.constants.ResaleTransactionStatus;
import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.resale.ResaleHoldEntity;
import com.goti.domain.entity.resale.ResaleListingEntity;
import com.goti.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.domain.entity.resale.ResaleTransactionEntity;
import com.goti.dto.request.ResaleTransactionRequest;
import com.goti.dto.response.ResaleTransactionInitResponse;
import com.goti.dto.response.ResaleTransactionSuccessResponse;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.repository.ResaleHoldRepository;
import com.goti.repository.ResaleRestrictionRepository;
import com.goti.repository.ResaleTransactionRepository;
import com.goti.repository.history.ResalePriceHistoryRepository;
import com.goti.repository.listing.ResaleListingRepository;
import com.goti.service.PaymentService;
import com.goti.utils.ResalePricePolicy;
import com.goti.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleTransactionService {
	private final ResaleListingRepository resaleListingRepository;
	private final ResaleRestrictionRepository resaleRestrictionRepository;
	private final ResaleTransactionRepository resaleTransactionRepository;
	private final ResalePriceHistoryRepository resalePriceHistoryRepository;
	private final ResaleHoldRepository resaleHoldRepository;
	private final ResaleRestrictionHandler resaleRestrictionHandler;
	private final ResalePricePolicy resalePricePolicy;
	private final PaymentService paymentService;

	@Transactional
	public ResaleTransactionInitResponse initTransaction(
		UUID buyerId,
		ResaleTransactionRequest request
	) {
		ResaleHoldEntity resaleHold = resaleHoldRepository
			.findByIdAndUserIdAndStatus(
				request.holdId(),
				buyerId,
				ResaleHoldStatus.HOLDING
			)
			.orElseThrow(
				() -> new CustomException(ErrorCode.RESALE_HOLD_NOT_FOUND)
			);

		Preconditions.validate(resaleHold.getExpiredAt().isAfter(LocalDateTime.now()), ErrorCode.RESALE_HOLD_EXPIRED);

		ResaleListingEntity resaleListing = resaleHold.getResaleListing();

		ResaleRestrictionEntity resaleRestriction = getOrCreateRestriction(buyerId);
		resaleRestrictionHandler.validateCanBuy(resaleRestriction, resaleListing.getGameId());

		ResalePricePolicy.FeeResult feeResult = resalePricePolicy.validateTransactionFee(
			resaleListing.getListingPrice()
		);

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
		resaleTransactionRepository.save(transaction);

		ResaleTransactionInitResponse paymentResponse = paymentService.createResalePayment(
			resaleListing.getId(),
			transaction.getId(),
			buyerId,
			feeResult.buyerTotal());

		//TODO : 결제 요청후 받는 이벤트

		log.info("리셀 완료 거래ID: {}, 구매자 비용: {}, 판매자 비용: {}"
			, transaction.getId(), feeResult.buyerTotal(), feeResult.sellerTotal());

		return paymentResponse;
	}

	// TODO : 점유상태가 끝나기 직전에 결제를 하면 오류가 발생 + 결제완료처리후 점유상태
	@Transactional
	public ResaleTransactionSuccessResponse completePayment(UUID transactionId, UUID escrowId) {
		ResaleTransactionEntity resaleTransaction = resaleTransactionRepository.findById(transactionId)
			.orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

		Preconditions.validate(resaleTransaction.getTransactionStatus() == ResaleTransactionStatus.PENDING,
			ErrorCode.NOT_MATCH_STATUS, "결제 대기");

		resaleTransaction.complete(escrowId);
		resaleTransactionRepository.save(resaleTransaction);

		ResaleListingEntity resaleListing = resaleTransaction.getListing();
		resaleListing.SoldOut(resaleTransaction.getTransactionPrice());
		resaleListingRepository.save(resaleListing);

		ResalePriceHistoryEntity resalePriceHistory = ResalePriceHistoryEntity.create(
			resaleListing.getGameId(),
			resaleListing.getSeatId(),
			resaleListing.getGradeId(),
			resaleTransaction.getTransactionPrice()
		);
		resalePriceHistoryRepository.save(resalePriceHistory);

		ResaleRestrictionEntity resaleRestriction = getOrCreateRestriction(resaleTransaction.getBuyerId());
		resaleRestrictionHandler.handleAfterBuy(resaleRestriction, resaleListing.getGameId());

		resaleRestrictionRepository.save(resaleRestriction);

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
		return resaleRestrictionRepository
			.findByUserId(userId)
			.orElseGet(
				() -> {
					ResaleRestrictionEntity newRestriction = ResaleRestrictionEntity.create(userId);
					return resaleRestrictionRepository.save(newRestriction);
				});
	}
}
