package com.goti.payment.service.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.payment.infra.ResaleOrderClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrderGateway {
	private final ResaleOrderClient resaleOrderClient;

	public void confirmResalePayment(
		UUID orderId,
		UUID buyerId,
		UUID paymentId
	) {
		log.info("리셀 주문 결제 확정 - orderId: {}, buyerId: {}", orderId, buyerId);
		resaleOrderClient.completeOrder(orderId, paymentId);
	}

	public List<UUID> getTransactionIds(UUID orderId) {
		return resaleOrderClient.getTransactionIds(orderId);
	}
}
