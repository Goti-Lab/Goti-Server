package com.goti.payment.service.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.payment.constants.PaymentMethod;
import com.goti.payment.constants.PaymentStatus;
import com.goti.constants.messages.ErrorCode;
import com.goti.payment.dto.response.PaymentResponse;
import com.goti.payment.infra.TicketingOrderClient;
import com.goti.global.validation.Preconditions;
import com.goti.payment.service.domain.PaymentService;
import com.goti.payment.service.dto.OrderPaymentConfirmApiRequest;
import com.goti.payment.service.dto.PaymentOrderInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {
	private final TicketingOrderClient ticketingOrderClient;
	private final PaymentService paymentService;

	@Transactional
	public PaymentResponse initPayment (
		UUID orderId,
		UUID memberId,
		PaymentMethod paymentMethod,
		String idempotencyKey
	) {
		PaymentOrderInfo order = ticketingOrderClient.getPaymentOrder(orderId, memberId);

		Preconditions.validate(
			order.memberId().equals(memberId),
			ErrorCode.AUTH_PERMISSION_DENIED
		);

		PaymentResponse payment = paymentService.create(
			order.orderId(),
			memberId,
			paymentMethod,
			idempotencyKey,
			order.totalAmount()
		);

		if (payment.paymentStatus() == PaymentStatus.SUCCESS) {
			ticketingOrderClient.confirmPayment(
				orderId,
				new OrderPaymentConfirmApiRequest(
					memberId,
					payment.paymentId(),
					payment.pgTid()
				)
			);
		}

		return payment;
	}

	@Transactional(readOnly = true)
	public PaymentResponse getByOrderId(UUID orderId, UUID memberId) {
		Preconditions.validate(
			memberId != null,
			ErrorCode.AUTH_INVALID
		);

		Preconditions.validate(
			orderId != null,
			ErrorCode.MISSING_PARAMETER,
			"orderId"
		);

		PaymentOrderInfo order = ticketingOrderClient.getPaymentOrder(orderId, memberId);
		Preconditions.validate(
			order.memberId().equals(memberId),
			ErrorCode.AUTH_PERMISSION_DENIED
		);

		return paymentService.getByOrderId(orderId);
	}
}
