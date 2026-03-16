package com.goti.service.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.constants.OrderStatus;
import com.goti.infra.ResaleOrderClient;
import com.goti.infra.TicketingOrderClient;
import com.goti.service.dto.OrderPaymentConfirmApiRequest;
import com.goti.service.dto.PaymentOrderInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrderGateway {
	private final TicketingOrderClient ticketingOrderClient;
	private final ResaleOrderClient resaleOrderClient;

	/*
	Ticketing
	 */
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

	/*
	Resale
	 */
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
