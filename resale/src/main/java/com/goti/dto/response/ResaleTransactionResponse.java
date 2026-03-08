package com.goti.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.constants.ResaleTransactionStatus;
import com.goti.domain.entity.resale.ResaleTransactionEntity;
import com.goti.utils.ResalePricePolicy;

public record ResaleTransactionResponse(
	UUID transactionId,
	UUID listingId,
	UUID buyerId,
	UUID sellerId,
	Integer transactionPrice,
	Integer buyerFee,
	Integer sellerFee,
	Integer buyerTotal,
	Integer sellerTotal,
	ResaleTransactionStatus status,
	LocalDateTime confirmedAt,
	String message
) {
	public static ResaleTransactionResponse from(ResaleTransactionEntity entity, String message) {
		ResalePricePolicy policy = new ResalePricePolicy();
		ResalePricePolicy.FeeResult feeResult = policy.validateTransactionFee(entity.getTransactionPrice());
		
		return new ResaleTransactionResponse(
			entity.getId(),
			entity.getListing().getId(),
			entity.getBuyerId(),
			entity.getSellerId(),
			entity.getTransactionPrice(),
			feeResult.buyerFee(),
			feeResult.sellerFee(),
			feeResult.buyerTotal(),
			feeResult.sellerTotal(),
			entity.getTransactionStatus(),
			entity.getConfirmedAt(),
			message
		);

	}

	public static ResaleTransactionResponse forBuyer(ResaleTransactionEntity entity) {
		String message = switch (entity.getTransactionStatus()) {
			case PENDING -> "결제 대기 중입니다. 결제를 완료해주세요.";
			case COMPLETED -> "구매가 완료되었습니다. 6시간 후 재판매가 가능합니다.";
		};
		return from(entity, message);
	}

	public static ResaleTransactionResponse forSeller(ResaleTransactionEntity entity) {
		String message = switch (entity.getTransactionStatus()) {
			case PENDING -> "구매자가 결제 중입니다.";
			case COMPLETED -> "판매가 완료되었습니다. 정산은 지정된 시간에 진행됩니다.";
		};
		return from(entity, message);
	}
}
