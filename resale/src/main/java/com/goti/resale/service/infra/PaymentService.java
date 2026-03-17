package com.goti.resale.service.infra;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.resale.dto.request.ResalePaymentRequest;
import com.goti.resale.dto.request.ResaleTransactionItemRequest;
import com.goti.resale.infra.PaymentClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
	private final PaymentClient paymentClient;

	public void createResalePayment(
		UUID orderId,
		UUID buyerId,
		int totalAmount,
		int totalBuyerFee,
		int totalSellerFee,
		List<ResaleTransactionItemRequest> items,
		String idempotencyKey
	) {
		ResalePaymentRequest request = new ResalePaymentRequest(
			orderId,
			buyerId,
			totalAmount,
			totalBuyerFee,
			totalSellerFee,
			items,
			"CARD",
			idempotencyKey
		);

		try {
			paymentClient.createResalePayment(request);
		} catch (Exception e) {
			log.error("리셀 결제 요청 실패 - orderId: {}, error: {}", orderId, e.getMessage());
			throw new CustomException(ErrorCode.RESALE_PAYMENT_FAILED);
		}
	}

	public void releaseEscrow(UUID orderId) {
		log.info("에스크로 해제 요청 - orderId: {}", orderId);
		try {
			paymentClient.releaseEscrow(orderId);
		} catch (Exception e) {
			log.error("에스크로 해제 실패 - orderId: {}, error: {}", orderId, e.getMessage());
			throw new CustomException(ErrorCode.RESALE_ESCROW_FAILED);
		}
	}
}