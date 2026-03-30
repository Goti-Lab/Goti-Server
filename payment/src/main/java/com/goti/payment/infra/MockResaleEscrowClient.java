package com.goti.payment.infra;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MockResaleEscrowClient implements ResaleEscrowClient {
	@Override
	public String requestEscrowPayment(UUID transactionId, Integer amount) {
		log.info("에스크로 결제 요청 - 거래 ID: {}, 금액: {}", transactionId, amount);
		return "ESCROW-" + UUID.randomUUID().toString().substring(0, 8);
	}

	@Override
	public void requestSettlement(String escrowId) {
		log.info("에스크로 정산 요청 - 에스크로 ID: {}", escrowId);
	}
}
