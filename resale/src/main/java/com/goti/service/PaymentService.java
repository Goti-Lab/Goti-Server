package com.goti.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.dto.response.ResaleTransactionInitResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment 도메인과의 통신 인터페이스 추후 RestClient 등으로 변경
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
	// TODO: 실제 구현 시 Payment 도메인과 통신
	public ResaleTransactionInitResponse createResalePayment(
		UUID listingId,
		UUID transactionId,
		UUID buyerId,
		Integer buyerTotal
	) {
		// 테스트용 더미 데이터
		log.info("티켓: {}", listingId);
		log.info("거래: {}", transactionId);
		log.info("구매자: {}", buyerId);
		log.info("구매자 가격: {}", buyerTotal);
		return ResaleTransactionInitResponse.page();
	}

	public boolean verifyPaymentCompletion(String paymentId) {
		log.info("결제 완료 확인 - paymentId: {}", paymentId);
		return true;

		/*
		TODO: 실제 구현 시 Payment 도메인에서 결제 상태 확인
		PaymentStatusResponse status = paymentClient.getPaymentStatus(paymentId);
		return status.isCompleted();
		*/
	}
}