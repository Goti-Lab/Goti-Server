package com.goti.payment.service.application;

import java.util.UUID;

import com.goti.payment.service.dto.PaymentOrderInfo;

import org.springframework.stereotype.Service;

import com.goti.constants.OrderStatus;
import com.goti.payment.infra.TicketingOrderClient;
import com.goti.payment.service.dto.OrderPaymentConfirmApiRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrderGateway {
	private final TicketingOrderClient ticketingOrderClient;

	public PaymentOrderInfo getPaymentOrder(
		UUID orderId,
		UUID memberId
	) {
		log.info("주문 조회 - orderId: {}, memberId: {}", orderId, memberId);

		// TODO: 실제 구현 시 ticketing/order 도메인 API 또는 RestClient로 조회
		return new PaymentOrderInfo(
			orderId,
			memberId,
			OrderStatus.PENDING,
			1000
		);
	}

	public void confirmPayment(
		UUID orderId,
		UUID memberId,
		UUID paymentId,
		String pgTid
	) {
		log.info("주문 결제 확정 - orderId: {}, memberId: {}", orderId, memberId);
		ticketingOrderClient.confirmPayment(
			orderId,
			new OrderPaymentConfirmApiRequest(memberId, paymentId, pgTid)
		);
	}
}
